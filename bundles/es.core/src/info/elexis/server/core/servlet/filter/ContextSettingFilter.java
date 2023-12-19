package info.elexis.server.core.servlet.filter;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.builder.IUserBuilder;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IUserService;
import ch.elexis.core.utils.OsgiServiceUtil;
import info.elexis.server.core.SystemPropertyConstants;

public class ContextSettingFilter implements Filter {

	private Logger logger;

	private IContextService contextService;
	private IModelService coreModelService;

	private LimitedLinkedHashMap<String, IUser> verificationCache;
	protected Pattern skipPattern;

	public ContextSettingFilter(IContextService contextService, IModelService coreModelService,
			String skipPatternDefinition) {
		this.contextService = contextService;
		this.coreModelService = coreModelService;
		logger = LoggerFactory.getLogger(getClass());
		verificationCache = new LimitedLinkedHashMap<>(100);
		if (skipPatternDefinition != null) {
			skipPattern = Pattern.compile(skipPatternDefinition, Pattern.DOTALL);
		}

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletResponse servletResponse = (HttpServletResponse) response;
		HttpServletRequest servletRequest = (HttpServletRequest) request;

		// info.elexis.server.core.internal.service.ContextService is ThreadLocal
		contextService.setActiveCoverage(null);
		contextService.setActiveMandator(null);
		contextService.setActiveUser(null);
		contextService.setActivePatient(null);

		if (shouldSkip(servletRequest)) {
			chain.doFilter(request, response);
			return;
		}

		// assert user and assignedContact are valid
		// adapt user roles to keycloak assigned roles
		KeycloakSecurityContext keycloakSecurityContext = (KeycloakSecurityContext) request
				.getAttribute(KeycloakSecurityContext.class.getName());
		if (keycloakSecurityContext != null) {
			AccessToken token = keycloakSecurityContext.getToken();
			String jti = token.getId();

			if (!verificationCache.containsKey(jti)) {
				Optional<IUser> _user = coreModelService.load(token.getPreferredUsername(), IUser.class);
				if (_user.isEmpty()) {
					_user = Optional.ofNullable(performDynamicUserCreationIfApplicable(token));
				}
				_user.ifPresent(u -> verificationCache.put(jti, u));

				IUser user = verificationCache.get(jti);
				if (user == null) {
					logger.warn("[{}] User not loadable in local database. Denying request.",
							token.getPreferredUsername());
					servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "");
					return;
				}

				IContact userContact = user != null ? user.getAssignedContact() : null;
				if (userContact == null) {
					logger.warn("[{}] User has no assigned contact. Denying request.", token.getPreferredUsername());
					servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "");
					return;
				}

				assertRoles(user, token);

				verificationCache.put(jti, user);
			}

			IUser user = verificationCache.get(jti);
			contextService.setActiveUser(user);
		} else {
			if (!SystemPropertyConstants.isDisableWebSecurity()) {
				throw new IllegalStateException("Web security enabled. No KeycloakContext found.");
			}
		}

		// TODO set selected mandator via request header

		chain.doFilter(request, response);
	}

	/**
	 * Dynamically creates user if applicable
	 * 
	 * @param token
	 * @return
	 */
	private IUser performDynamicUserCreationIfApplicable(AccessToken token) {
		boolean isElexisUser = token.getRealmAccess().getRoles().contains("bot")
				|| token.getRealmAccess().getRoles().contains("user");
		if (!isElexisUser) {
			return null;
		}
		// if an elexisContactId is set, and it is valid - dynamically create user
		String elexisContactId = (String) token.getOtherClaims().get("elexisContactId");
		Optional<IContact> assignedContact = coreModelService.load(elexisContactId, IContact.class);
		if (!assignedContact.isPresent()) {
			logger.warn("[{}] Dynamic user create failed. Invalid or missing attribute elexisContactId [{}]",
					token.getPreferredUsername(), elexisContactId);
			return null;
		}
		logger.info("[{}] Dynamic user/bot create with assigned contact [{}]", token.getPreferredUsername(),
				elexisContactId);
		return new IUserBuilder(coreModelService, token.getPreferredUsername(), assignedContact.get()).buildAndSave();
	}

	/**
	 * If Keycloak grants a user specific roles, we set this as new role total for
	 * the user
	 * 
	 * @param accessControlService
	 * @param user
	 * @param token
	 */
	private void assertRoles(IUser user, AccessToken token) {
		Set<String> keycloakGrantedRoles = token.getRealmAccess().getRoles();
		Set<String> allAvailableRoles = coreModelService.getQuery(IRole.class).execute().stream().map(r -> r.getId())
				.collect(Collectors.toSet());

		Set<String> targetUserRoleSet = new HashSet<String>(allAvailableRoles);
		targetUserRoleSet.retainAll(keycloakGrantedRoles);
		Set<String> currentUserRoleSet = user.getRoles().stream().map(r -> r.getId()).collect(Collectors.toSet());

		if (!Objects.equals(currentUserRoleSet, targetUserRoleSet)) {
			IUserService userService = OsgiServiceUtil.getService(IUserService.class).get();
			Set<String> effectiveUserRoles = userService.setUserRoles(user, targetUserRoleSet);
			logger.warn("[{}] Updated user/bot role set to {}", user, effectiveUserRoles);
		}
	}

	@Override
	public void destroy() {
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

	/**
	 * @see org.keycloak.adapters.servlet.KeycloakOIDCFilter#shouldSkip
	 */
	private boolean shouldSkip(HttpServletRequest request) {

		if (skipPattern == null) {
			return false;
		}

		String requestPath = request.getRequestURI().substring(request.getContextPath().length());
		return skipPattern.matcher(requestPath).matches();
	}

	private class LimitedLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

		private static final long serialVersionUID = -4811170640063577667L;
		private final int maxSize;

		public LimitedLinkedHashMap(int maxSize) {
			super(16, 0.75f, false);
			this.maxSize = maxSize;
		}

		@Override
		protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
			return size() > maxSize;
		}
	}

}
