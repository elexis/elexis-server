package info.elexis.server.core.common.security;

import org.apache.shiro.realm.Realm;
import org.apache.shiro.util.Nameable;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.authz.AuthorizationInfo;

/**
 * This interface solely exists in order to get public access rights to the
 * included methods.
 */
public interface ESAuthorizingRealm extends Realm, Nameable {

	public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException;

	public AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals);

	public CredentialsMatcher getCredentialsMatcher();
}
