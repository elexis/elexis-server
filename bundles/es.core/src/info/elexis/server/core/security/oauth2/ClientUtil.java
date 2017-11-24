package info.elexis.server.core.security.oauth2;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.security.oauth2.internal.ClientAuthenticationFile;

public class ClientUtil {

	private static Logger log = LoggerFactory.getLogger(ClientUtil.class);

	private static final DefaultPasswordService defaultPasswordService = new DefaultPasswordService();

	/**
	 * Adds or replaces an OAuth Client registration. Every time this method is
	 * called the the client secret is replaced.
	 * 
	 * @param clientId
	 * @return the client secret to use
	 * @throws IOException
	 */
	public static String addOrReplaceOauthClient(String clientId, Set<String> roles) throws IOException {
		String password = UUID.fromString(UUID.nameUUIDFromBytes(UUID.randomUUID().toString().getBytes()).toString())
				.toString();
		ClientAuthenticationFile.getInstance().addOrReplaceId(clientId, roles, password);
		return password;
	}

	/**
	 * Validate a provided OAuth client id and client secret.
	 * 
	 * @param clientId
	 * @param clientSecret
	 * @return <code>true</code> if valid
	 */
	public static boolean validateClient(String clientId, String clientSecret) {
		if (StringUtils.isNotEmpty(clientId) && StringUtils.isNotEmpty(clientSecret)) {
			String hashedPasswordForClient = ClientAuthenticationFile.getInstance()
					.getHashedPasswordForClient(clientId);
			return defaultPasswordService.passwordsMatch(clientSecret, hashedPasswordForClient);
		}

		return false;
	}

	/**
	 * Validate whether the provided OAuth client allows all requested scopes
	 * 
	 * @param clientId
	 * @param scopes
	 * @return <code>true</code> if valid
	 */
	public static boolean validateRequestedScopes(String clientId, Set<String> scopes) {
		Set<String> rolesForClientId = ClientAuthenticationFile.getInstance().getRolesForClientId(clientId);
		for (String scope : scopes) {
			if (!rolesForClientId.contains(scope)) {
				log.warn("Client [{}] requested non-granted scope [{}], stopping evaluation.", clientId, scope);
				return false;
			}
		}
		return true;
	}
}
