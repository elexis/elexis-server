package info.elexis.server.core.security.oauth2;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticatingResourceFilter extends AuthenticatingFilter {

	private Logger log = LoggerFactory.getLogger(AuthenticatingResourceFilter.class);

	@Override
	protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		OAuthAccessResourceRequest oAuthAccessResourceRequest = new OAuthAccessResourceRequest(httpServletRequest);
		String accessToken = oAuthAccessResourceRequest.getAccessToken();
		return new OAuth2Token(accessToken, httpServletRequest);
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		WebUtils.toHttp(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
		return false;
	}

	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;

		// Check if there is an Authorization Header
		String authzHeader = httpServletRequest.getHeader(OAuth.HeaderType.AUTHORIZATION);
		if (!OAuthUtils.isEmpty(authzHeader)) {
			try {
				OAuthAccessResourceRequest oAuthAccessResourceRequest = new OAuthAccessResourceRequest(
						httpServletRequest);
				String accessToken = oAuthAccessResourceRequest.getAccessToken();

				if (StringUtils.hasLength(accessToken)) {
					// Authentication headers requests should not create sessions.
					request.setAttribute(DefaultSubjectContext.SESSION_CREATION_ENABLED, Boolean.FALSE);

					return executeLogin(request, response); // calls #createToken
				}
			} catch (Exception e) {
				log.warn("isAccessAllowed", e);
			}
		}

		return isPermissive(mappedValue);
	}

}
