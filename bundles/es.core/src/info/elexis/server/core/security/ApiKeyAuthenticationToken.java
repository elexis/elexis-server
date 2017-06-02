package info.elexis.server.core.security;

import org.apache.shiro.authc.AuthenticationToken;

public class ApiKeyAuthenticationToken implements AuthenticationToken {

	private static final long serialVersionUID = 2365178859086941932L;

	private final String apiKey;

    public ApiKeyAuthenticationToken(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public Object getCredentials() {
        return this.apiKey;
    }

    @Override
    public Object getPrincipal() {
        return "apiKey";
    }

}
