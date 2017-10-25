package info.elexis.server.core.security;

import java.io.IOException;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.osgi.service.component.annotations.Component;

import info.elexis.server.core.common.security.ESAuthorizingRealm;
import info.elexis.server.core.internal.security.ElexisServerAuthenticationFile;

/**
 * Elexis-Server local authorizing realm, stored in
 * ${user.home}/elexis-server/elxis-server-auth.conf
 */
@Component(service = ESAuthorizingRealm.class)
public class SystemLocalAuthorizingRealm extends AuthorizingRealm implements ESAuthorizingRealm {

	public static final String REALM_NAME = "elexis-server.local";

	static {
		ElexisServerAuthenticationFile.loadFile();
	}

	public SystemLocalAuthorizingRealm() {
		super(new PasswordMatcher());
		setName(REALM_NAME);
	}

	@Override
	public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

		if (token instanceof UsernamePasswordToken) {
			UsernamePasswordToken upToken = (UsernamePasswordToken) token;
			String userid = upToken.getUsername();
			if (userid == null || userid.length() == 0) {
				return null;
			}

			String hashedPassword = ElexisServerAuthenticationFile.getHashedPasswordForUserId(userid);
			if (hashedPassword != null) {
				SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(userid, hashedPassword, REALM_NAME);
				return info;
			}
		} else if (token instanceof ApiKeyAuthenticationToken) {
			String apiKey = (String) ((ApiKeyAuthenticationToken) token).getCredentials();
			String user = ElexisServerAuthenticationFile.getUserByApiKey(apiKey);
			if (user != null) {

			}
		}

		return null;
	}

	@Override
	public AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		String userid = (String) getAvailablePrincipal(principals);

		Set<String> roles = ElexisServerAuthenticationFile.getRolesForUserId(userid);

		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roles);
		return info;
	}

	public static boolean localRealmIsInitialized() {
		return ElexisServerAuthenticationFile.isInitialized();
	}

	/**
	 * 
	 * @param password
	 * @return an apiKey generated for the esadmin user
	 * @throws IOException
	 */
	public static String setInitialEsAdminPassword(String password) throws IOException {
		return ElexisServerAuthenticationFile.setInitialEsAdminPassword(password);
	}

	public static void clearRealm() throws IOException {
		ElexisServerAuthenticationFile.clearAndRemove();
	}

}
