package info.elexis.server.core.connector.elexis.security;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.apache.commons.codec.DecoderException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rgw.tools.PasswordEncryptionService;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.User;

/**
 * Match credentials against the Elexis database, that is, against the
 * {@link User} table
 */
public class ElexisCredentialsMatcher implements CredentialsMatcher {

	private static Logger log = LoggerFactory.getLogger(ElexisCredentialsMatcher.class);
	private static PasswordEncryptionService pes = new PasswordEncryptionService();

	@Override
	public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
		UsernamePasswordToken upToken = (UsernamePasswordToken) token;

		SimpleAuthenticationInfo sai = (SimpleAuthenticationInfo) info;
		User user = (User) sai.getCredentials();
		String hashedPassword = user.getHashedPassword();
		String salt = user.getSalt();

		try {
			log.debug("Validating user {} [{}]", upToken.getUsername(), user.getId());
			String password = new String(upToken.getPassword());
			return pes.authenticate(password, hashedPassword, salt);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | DecoderException e) {
			log.error("Error authenticating user " + upToken.getUsername(), e);
		}

		return false;
	}

}
