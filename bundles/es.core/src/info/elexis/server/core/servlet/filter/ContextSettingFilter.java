package info.elexis.server.core.servlet.filter;

import java.io.IOException;
import java.util.regex.Pattern;

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
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import info.elexis.server.core.SystemPropertyConstants;

public class ContextSettingFilter implements Filter {
	
	private IContextService contextService;
	private IModelService coreModelService;
	
	protected Pattern skipPattern;
	
	private Logger logger;
	
	public ContextSettingFilter(IContextService contextService, IModelService coreModelService,
		String skipPatternDefinition){
		this.contextService = contextService;
		this.coreModelService = coreModelService;
		logger = LoggerFactory.getLogger(getClass());
		if (skipPatternDefinition != null) {
			skipPattern = Pattern.compile(skipPatternDefinition, Pattern.DOTALL);
		}
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException{
		
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
		KeycloakSecurityContext keycloakSecurityContext =
			(KeycloakSecurityContext) request.getAttribute(KeycloakSecurityContext.class.getName());
		if (keycloakSecurityContext != null) {
			AccessToken token = keycloakSecurityContext.getToken();
			
			String preferredUsername = token.getPreferredUsername();
			IUser user = coreModelService.load(preferredUsername, IUser.class).orElse(null);
			IContact userContact = (user != null) ? user.getAssignedContact() : null;
			
			if (user == null) {
				logger.warn("User [{}] not loadable in local database. Denying request.",
					preferredUsername);
				servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "");
				return;
			}
			
			if (userContact == null) {
				logger.warn("User [{}] has no assigned contact. Denying request.",
					preferredUsername);
				servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "");
				return;
			}
			
			contextService.setActiveUser(user);
		} else {
			if (!SystemPropertyConstants.isDisableWebSecurity()) {
				throw new IllegalStateException("Web security enabled. No KeycloakContext found.");
			}
		}
		
		// TODO set selected mandator via request header
		
		chain.doFilter(request, response);
	}
	
	@Override
	public void destroy(){}
	
	@Override
	public void init(FilterConfig arg0) throws ServletException{}
	
	/**
	 * @see org.keycloak.adapters.servlet.KeycloakOIDCFilter#shouldSkip
	 */
	private boolean shouldSkip(HttpServletRequest request){
		
		if (skipPattern == null) {
			return false;
		}
		
		String requestPath = request.getRequestURI().substring(request.getContextPath().length());
		return skipPattern.matcher(requestPath).matches();
	}
	
}
