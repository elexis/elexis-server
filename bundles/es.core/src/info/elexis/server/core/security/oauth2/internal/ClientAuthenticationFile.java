package info.elexis.server.core.security.oauth2.internal;

import java.nio.file.Path;

import info.elexis.server.core.common.util.CoreUtil;
import info.elexis.server.core.security.internal.AbstractAuthenticationFile;

/**
 * Authentication file containing OAuth2 Clients
 * 
 */
public class ClientAuthenticationFile extends AbstractAuthenticationFile {

	public static final String FILENAME = OAuthAuthorizingRealm.REALM_NAME + ".auth";
	public static final Path AUTH_FILE_PATH = CoreUtil.getHomeDirectory().resolve(FILENAME);

	private static ClientAuthenticationFile INSTANCE;

	private ClientAuthenticationFile() {
		super(AUTH_FILE_PATH, ':', "Elexis-Server OAuth Client Authentication");
		loadFile();
	}

	public static ClientAuthenticationFile getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ClientAuthenticationFile();
		}
		return INSTANCE;
	}

}
