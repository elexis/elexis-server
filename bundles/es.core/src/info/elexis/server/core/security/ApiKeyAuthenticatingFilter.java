package info.elexis.server.core.security;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;

import info.elexis.server.core.security.ApiKeyAuthenticationToken;

public class ApiKeyAuthenticatingFilter extends AuthenticatingFilter {

	@Override
	protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
		AuthenticationToken token = createToken(request, response);
		if (token != null) {
			Subject subject = getSubject(request, response);
			subject.login(token);
			return onLoginSuccess(token, subject, request, response);
		}
		return false;
	}

	@Override
	protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		String accessToken = httpServletRequest.getHeader("X-Api-Key");
		if (StringUtils.hasText(accessToken)) {
			return new ApiKeyAuthenticationToken(accessToken);
		}
		return null;
	}

	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
		// Shiro calls this method and the default implementation checks whether
		// the subject is authenticated.
		// It will never be, so overwriting the default method to prevent
		// unnecessary method calls.
		return false;
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		String authenticationHeader = ((HttpServletRequest) request).getHeader("X-Api-Key");

		if (authenticationHeader != null || true) {
			// Authentication headers requests should not create sessions.
			request.setAttribute(DefaultSubjectContext.SESSION_CREATION_ENABLED, Boolean.FALSE);

			executeLogin(request, response);
		}

		// Allow the request to go the next filters.
		return true;
	}

}