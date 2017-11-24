package info.elexis.server.core.security.oauth2;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;

public class AuthenticatingResourceFilter extends AuthenticatingFilter {

	@Override
	protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		OAuthAccessResourceRequest oAuthAccessResourceRequest = new OAuthAccessResourceRequest(httpServletRequest);
		String accessToken = oAuthAccessResourceRequest.getAccessToken();
		if (StringUtils.hasText(accessToken)) {
			return new OAuth2Token(accessToken, httpServletRequest);
		}
		return null;
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		OAuthAccessResourceRequest oAuthAccessResourceRequest = new OAuthAccessResourceRequest(httpServletRequest);
		String accessToken = oAuthAccessResourceRequest.getAccessToken();

		if (StringUtils.hasLength(accessToken)) {
			// Authentication headers requests should not create sessions.
			request.setAttribute(DefaultSubjectContext.SESSION_CREATION_ENABLED, Boolean.FALSE);

			executeLogin(request, response);
		}

		// Allow the request to go the next filters.
		return true;
	}

	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
		// Shiro calls this method and the default implementation checks whether
		// the subject is authenticated.
		// It will never be, so overwriting the default method to prevent
		// unnecessary method calls.
		return false;
	}

}
