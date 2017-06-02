package info.elexis.server.core.common.security;

/**
 * This interface solely exists in order to get public access rights to the
 * included methods.
 */
public interface ESAuthorizingRealm extends org.apache.shiro.realm.Realm, org.apache.shiro.util.Nameable {

	public org.apache.shiro.authc.AuthenticationInfo doGetAuthenticationInfo(
			org.apache.shiro.authc.AuthenticationToken token) throws org.apache.shiro.authc.AuthenticationException;

	public org.apache.shiro.authz.AuthorizationInfo doGetAuthorizationInfo(
			org.apache.shiro.subject.PrincipalCollection principals);

	public org.apache.shiro.authc.credential.CredentialsMatcher getCredentialsMatcher();
}
