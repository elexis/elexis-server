package info.elexis.server.core.security.oauth2.internal;

import java.util.Set;

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

/**
 * Authorizing Realm for {@link OAuth2Token}
 */
@Component(service = ESAuthorizingRealm.class)
public class OAuthAuthorizingRealm extends AuthorizingRealm implements ESAuthorizingRealm {

	public static final String REALM_NAME = "elexis-server.oauth2";
	private static final OAuthService oAuthService = new OAuthService();

	public OAuthAuthorizingRealm() {
		super(new CredentialsMatcher() {
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
		String primaryPrincipal = (String) principals.getPrimaryPrincipal();
		Set<String> scopes = oAuthService.getScopes(primaryPrincipal);
		authzInfo.setRoles(scopes);
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
