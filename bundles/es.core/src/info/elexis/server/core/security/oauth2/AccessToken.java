package info.elexis.server.core.security.oauth2;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authc.AuthenticationToken;

public class AccessToken implements AuthenticationToken {

	private static final long serialVersionUID = -757378077275108680L;
	
	private final String access_token;
	private final HttpServletRequest httpServletRequest;
	
	public AccessToken(String access_token, HttpServletRequest httpServletRequest) {
		this.access_token = access_token;
		this.httpServletRequest = httpServletRequest;
	}

	@Override
	public Object getPrincipal() {
		return this.access_token;
	}

	@Override
	public Object getCredentials() {
		return this.access_token;
	}

	public HttpServletRequest getHttpServletRequest() {
		return httpServletRequest;
	}
}
