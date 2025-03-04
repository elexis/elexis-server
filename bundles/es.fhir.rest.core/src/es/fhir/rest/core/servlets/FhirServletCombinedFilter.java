package es.fhir.rest.core.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;

import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.utils.OsgiServiceUtil;
import info.elexis.server.core.SystemPropertyConstants;
import info.elexis.server.core.servlet.filter.ContextSettingFilter;
import io.curity.oauth.OAuthJwtFilter;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.http.HttpServletRequest;

@WebFilter(urlPatterns = { "/fhir/*" }, initParams = { @WebInitParam(name = "skipPattern", value = "/fhir/metadata") })
public class FhirServletCombinedFilter implements Filter {

	private ContextSettingFilter contextSettingFilter;
	private FilterConfig filterConfig;

	private Pattern skipPattern;
	private OAuthJwtFilter oAuthJwtFilter;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;

		if (SystemPropertyConstants.isDisableWebSecurity()) {
			LoggerFactory.getLogger(getClass()).error("!!! UNPROTECTED FHIR API !!!");
		} else {
			String skipPatternDefinition = filterConfig.getInitParameter("skipPattern");
			if (skipPatternDefinition != null) {
				skipPattern = Pattern.compile(skipPatternDefinition, Pattern.DOTALL);
			}
			initializeOAuthFilter();
			LoggerFactory.getLogger(getClass()).debug("Filter initialized");
		}

	}

	private void initializeOAuthFilter() throws ServletException {
		oAuthJwtFilter = new OAuthJwtFilter();
		oAuthJwtFilter.init(new EnvironmentVariablesExtendedFilterConfig(filterConfig));
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (!SystemPropertyConstants.isDisableWebSecurity()) {
			HttpServletRequest servletRequest = (HttpServletRequest) request;
			if (shouldSkip(servletRequest)) {
				chain.doFilter(request, response);
				return;
			}
			oAuthJwtFilter.doFilter(servletRequest, response, chain);

			if (contextSettingFilter == null) {
				IModelService coreModelService = OsgiServiceUtil.getService(IModelService.class,
						"(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();
				IContextService contextService = OsgiServiceUtil.getService(IContextService.class).get();
				IAccessControlService accessControlService = OsgiServiceUtil.getService(IAccessControlService.class)
						.get();
				contextSettingFilter = new ContextSettingFilter(contextService, coreModelService, accessControlService,
						null);
			}

			contextSettingFilter.doFilter(servletRequest, response, chain);
		}

		chain.doFilter(request, response);
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

	/**
	 * Consider environment variables as well as init parameters for FilterConfig
	 */
	private class EnvironmentVariablesExtendedFilterConfig implements FilterConfig {

		private static final String ENV_PREFIX = "OAUTH_FILTER_";

		private FilterConfig filterConfig;

		public EnvironmentVariablesExtendedFilterConfig(FilterConfig filterConfig) {
			this.filterConfig = filterConfig;
		}

		@Override
		public String getFilterName() {
			return filterConfig.getFilterName();
		}

		@Override
		public String getInitParameter(String name) {
			String initParameter = filterConfig.getInitParameter(name);
			if (initParameter == null) {
				initParameter = System.getenv(ENV_PREFIX + name);
			}
			return initParameter;
		}

		@Override
		public Enumeration<String> getInitParameterNames() {
			Enumeration<String> initParameterNames = filterConfig.getInitParameterNames();
			List<String> list = System.getenv().keySet().stream().filter(key -> key.startsWith(ENV_PREFIX))
					.map(key -> key.substring(ENV_PREFIX.length())).toList();

			ArrayList<String> lt = new ArrayList<>(list);
			initParameterNames.asIterator().forEachRemaining(lt::add);
			return Collections.enumeration(lt);
		}

		@Override
		public ServletContext getServletContext() {
			return filterConfig.getServletContext();
		}

	}

}
