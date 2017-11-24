package info.elexis.server.core.security.oauth2.internal;

import java.util.Collections;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.osgi.service.component.annotations.Component;

import info.elexis.server.core.common.security.ESAuthorizingRealm;
import info.elexis.server.core.security.oauth2.OAuth2Token;

@Component(service = OAuthAuthorizingRealm.class)
public class OAuthAuthorizingRealm extends AuthorizingRealm implements ESAuthorizingRealm {

	public static final String REALM_NAME = "elexis-server.oauth2";

	public OAuthAuthorizingRealm() {
		super(new CredentialsMatcher() {
			private final OAuthService oAuthService = new OAuthService();

			@Override
			public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
				if (token instanceof OAuth2Token) {
					boolean result = oAuthService.checkAccessToken((String) token.getCredentials(),
							((OAuth2Token) token).getHttpServletRequest());
					return result;
				}
				return false;
			}
		});
		setName(REALM_NAME);
	}

	@Override
	public AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		SimpleAuthorizationInfo authzInfo = new SimpleAuthorizationInfo();
		authzInfo.setRoles(Collections.emptySet()); // TODO
		return authzInfo;
	}

	@Override
	public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		if (token instanceof OAuth2Token) {
			return new SimpleAuthenticationInfo((String) token.getPrincipal(), null, REALM_NAME);
		}
		return null;
	}

	@Override
	public boolean supports(AuthenticationToken token) {
		return (token instanceof OAuth2Token);
	}
}
