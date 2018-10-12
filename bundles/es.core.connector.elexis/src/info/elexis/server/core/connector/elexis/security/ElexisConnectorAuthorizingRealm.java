package info.elexis.server.core.connector.elexis.security;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.codec.DecoderException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.SimpleByteSource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IModelService;
import ch.rgw.tools.PasswordEncryptionService;
import info.elexis.server.core.common.security.ESAuthorizingRealm;

/**
 * Realm to authenticate and authorize against an Elexis >=3.2 database.
 */
@Component(service = ESAuthorizingRealm.class)
public class ElexisConnectorAuthorizingRealm extends AuthorizingRealm
		implements ESAuthorizingRealm {
	
	public static final String REALM_NAME = "elexis-connector";
	
	private static Logger log = LoggerFactory.getLogger(ElexisConnectorAuthorizingRealm.class);
	
	public ElexisConnectorAuthorizingRealm(){
		super(credentialsMatcher);
		setName(REALM_NAME);
	}
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;
	
	private static final CredentialsMatcher credentialsMatcher = new CredentialsMatcher() {
		
		private final PasswordEncryptionService pes = new PasswordEncryptionService();
		
		@Override
		public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info){
			SimpleAuthenticationInfo sai = (SimpleAuthenticationInfo) info;
			
			if (token instanceof UsernamePasswordToken) {
				UsernamePasswordToken upToken = (UsernamePasswordToken) token;
				
				try {
					// #5811
					String attemptedPassword = new String(upToken.getPassword());
					String credentials = sai.getCredentials().toString();
					String salt = new String(sai.getCredentialsSalt().getBytes());
					return pes.authenticate(attemptedPassword, credentials, salt);
				} catch (NoSuchAlgorithmException | InvalidKeySpecException | DecoderException e) {
					log.warn("Error verifying password for user [{}].", e);
				}
			}
			
			return false;
		}
	};
	
	@Override
	public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
		throws AuthenticationException{
		
		Optional<IUser> userOptional = Optional.empty();
		boolean validUser = false;
		
		if (token instanceof UsernamePasswordToken) {
			UsernamePasswordToken upToken = (UsernamePasswordToken) token;
			String userid = upToken.getUsername();
			if (userid == null || userid.length() == 0) {
				return null;
			}
			
			userOptional = modelService.load(userid, IUser.class);
			if (userOptional.isPresent()) {
				validUser = (userid.equals(userOptional.get().getId()));
				if (!validUser) {
					log.info("userid does not match [{}] : [{}]", userid,
						userOptional.get().getId());
				}
			}
			
		}
		
		if (userOptional.isPresent()) {
			IUser user = userOptional.get();
			String hashedPassword = user.getHashedPassword();
			String salt = user.getSalt();
			SimpleAuthenticationInfo authenticationInfo =
				new SimpleAuthenticationInfo(user.getId(), hashedPassword, REALM_NAME);
			authenticationInfo.setCredentialsSalt(new SimpleByteSource(salt));
			return authenticationInfo;
		}
		
		return null;
	}
	
	@Override
	public AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals){
		String userid = (String) getAvailablePrincipal(principals);
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		
		Optional<IUser> userOptional = modelService.load(userid, IUser.class);
		if (userOptional.isPresent()) {
			IUser user = userOptional.get();
			Set<String> roles =
				user.getRoles().stream().map(r -> r.getId()).collect(Collectors.toSet());
			info.setRoles(roles);
		}
		return info;
	}
	
	@Override
	public boolean supports(AuthenticationToken token){
		return (token instanceof UsernamePasswordToken);
	}
}
