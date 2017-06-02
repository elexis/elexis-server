package info.elexis.server.core.security;

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
import info.elexis.server.core.security.internal.ElexisServerAuthenticationFile;

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

	public static void setInitialEsAdminPassword(String password) {
		ElexisServerAuthenticationFile.setInitialEsAdminPassword(password);
	}

}
