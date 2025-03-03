package info.elexis.jaxrs.service.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;

import io.curity.oauth.AuthenticatedUser;
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

@WebFilter(urlPatterns = { "/services/*" }, initParams = {
		@WebInitParam(name = "skipPattern", value = "/services/(elexis|public)/.*") })
public class JaxRsJerseyServletCombinedFilter implements Filter {

	private Pattern skipPattern;
	private FilterConfig filterConfig;

	private OAuthJwtFilter oAuthJwtFilter;

	private static final boolean IS_DISABLE_WEBSEC = Boolean.valueOf(System.getProperty("disable.web.security"));

	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;

		if (IS_DISABLE_WEBSEC) {
			LoggerFactory.getLogger(getClass()).error("!!! UNPROTECTED SERVICES API !!!");
		} else {
			String skipPatternDefinition = filterConfig.getInitParameter("skipPattern");
			if (skipPatternDefinition != null) {
				skipPattern = Pattern.compile(skipPatternDefinition, Pattern.DOTALL);
			}
			initializeFilters();
			LoggerFactory.getLogger(getClass()).debug("Filter initialized");
		}

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (!IS_DISABLE_WEBSEC) {
			HttpServletRequest servletRequest = (HttpServletRequest) request;
			if (shouldSkip(servletRequest)) {
				chain.doFilter(request, response);
				return;
			}
			oAuthJwtFilter.doFilter(servletRequest, response, chain);
//			contextSettingFilter.doFilter(servletRequest, response, chain);

			AuthenticatedUser authenticatedUser = (AuthenticatedUser) servletRequest.getAttribute("principal");
			String jti = authenticatedUser.getClaim("jti").getAsString();
			Long exp = authenticatedUser.getClaim("exp").getAsLong();
			String preferredUsername = authenticatedUser.getClaim("preferred_username").getAsString();
			JsonArray roles = authenticatedUser.getClaim("realm_access").getAsJsonObject().get("roles")
					.getAsJsonArray();
			System.out.println(jti);
			System.out.println(exp);
			System.out.println(preferredUsername);
			System.out.println(roles);
			System.out.println(
					"Hello " + authenticatedUser.getClaim("preferred_username").getAsString() + " welcome home");
		}

		chain.doFilter(request, response);
	}

	private void initializeFilters() throws ServletException {
		oAuthJwtFilter = new OAuthJwtFilter();
		oAuthJwtFilter.init(new EnvironmentVariablesExtendedFilterConfig(filterConfig));
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
