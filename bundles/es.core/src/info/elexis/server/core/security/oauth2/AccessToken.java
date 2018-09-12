package info.elexis.server.core.security.oauth2;

import org.apache.shiro.authc.HostAuthenticationToken;

public class AccessToken implements HostAuthenticationToken {

	private static final long serialVersionUID = -757378077275108680L;

	private final String access_token;
	private final String host;
	private String userId = "unknown";
	
	public AccessToken(String access_token, String host) {
		this.access_token = access_token;
		this.host = host;
	}

	@Override
	public Object getPrincipal() {
		return this.userId;
	}

	@Override
	public Object getCredentials() {
		return this.access_token;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public String getHost() {
		return host;
	}
	
	@Override
	public String toString() {
		return getPrincipal()+"@"+getHost();
	}
	
}
