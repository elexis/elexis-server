package info.elexis.server.core.security.oauth2.internal;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.common.security.ESAuthorizingRealm;
import info.elexis.server.core.security.oauth2.AccessToken;

/**
 * Authorizing Realm for {@link AccessToken}
 */
@Component(service = ESAuthorizingRealm.class)
public class OAuth2AuthorizingRealm extends AuthorizingRealm implements ESAuthorizingRealm {

	private Logger log = LoggerFactory.getLogger(OAuth2AuthorizingRealm.class);

	public static final String REALM_NAME = "elexis-server.oauth2";
	private static final OAuth2ClientService oidClientService = new OAuth2ClientService();

	public OAuth2AuthorizingRealm() {
		super(new CredentialsMatcher() {
			@Override
			public boolean doCredentialsMatch(AuthenticationToken accessToken, AuthenticationInfo info) {
				if (accessToken instanceof AccessToken) {
					boolean result = oidClientService.checkAccessToken((String) accessToken.getCredentials());
					return result;
				}
				return false;
			}
		});
		setName(REALM_NAME);
	}

	@Override
	public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken accessToken) throws AuthenticationException {
		if (accessToken instanceof AccessToken) {
			// enrich Access Token with username
			OAuth2AccessToken primaryPrincipal = oidClientService
					.getIntrospectionToken((String) accessToken.getCredentials());
			((AccessToken) accessToken).setUserId(primaryPrincipal.getUserId());
			
			return new SimpleAuthenticationInfo(accessToken, null, REALM_NAME);
		}
		return null;
	}

	@Override
	public AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		SimpleAuthorizationInfo authzInfo = new SimpleAuthorizationInfo();

		AccessToken accessToken = (AccessToken) principals.getPrimaryPrincipal();
		OAuth2AccessToken primaryPrincipal = oidClientService
				.getIntrospectionToken((String) accessToken.getCredentials());

		validateScopesLocal(primaryPrincipal, authzInfo);

		return authzInfo;
	}

	/**
	 * Currently OpenID will simply allow all scopes and distribute them, so we have
	 * to check locally at the resource whether the are really valid. In order to do
	 * this we need a valid user, which precludes client_credentials flow.<br>
	 * <br>
	 * Scopes that are not requested, will not be automatically granted. If a user
	 * e.g. has roles a, b and c. A request with scope a will only lead to this
	 * single scope being granted.
	 * 
	 * @param token
	 * @param authzInfo
	 * @see https://github.com/mitreid-connect/OpenID-Connect-Java-Spring-Server/issues/351
	 */
	private void validateScopesLocal(OAuth2AccessToken token, SimpleAuthorizationInfo authzInfo) {
		// TODO support REMOTE_ALLOWED https://redmine.medelexis.ch/issues/10926
		String userId = token.getUserId();
		if (userId == null) {
			log.warn("Token [{}] did not contain user_id, no scopes granted.", token.getValue());
		} else {
			for (String scope : token.getScope()) {
				SimplePrincipalCollection simplePrincipalCollection = new SimplePrincipalCollection(userId,
						"elexis-connector");

				boolean hasRole = SecurityUtils.getSecurityManager().hasRole(simplePrincipalCollection, scope);
				if (hasRole) {
					authzInfo.addRole(scope);
				} else {
					log.warn("User [{}] requested scope [{}] not backed by role. Denying request.", userId, scope);
				}
			}
		}

	}

	@Override
	public boolean supports(AuthenticationToken token) {
		return (token instanceof AccessToken);
	}
}
