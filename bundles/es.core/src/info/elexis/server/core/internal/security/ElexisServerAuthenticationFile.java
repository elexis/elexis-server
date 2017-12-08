package info.elexis.server.core.internal.security;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

import info.elexis.server.core.common.util.CoreUtil;
import info.elexis.server.core.constants.SecurityConstants;
import info.elexis.server.core.security.SystemLocalAuthorizingRealm;

public class ElexisServerAuthenticationFile extends AbstractAuthenticationFile {

	public static final String FILENAME = SystemLocalAuthorizingRealm.REALM_NAME + ".auth";
	public static final Path AUTH_FILE_PATH = CoreUtil.getHomeDirectory().resolve(FILENAME);

	private static ElexisServerAuthenticationFile INSTANCE;

	private ElexisServerAuthenticationFile() {
		super(AUTH_FILE_PATH, ':', "Elexis-Server User Authentication");
		loadFile();
	}

	public static ElexisServerAuthenticationFile getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ElexisServerAuthenticationFile();
		}
		return INSTANCE;
	}

	/**
	 * Sets the initial password of the {@link ElexisServerWebConstants#ES_ADMIN}
	 * user. Can be set only once. In order to be able to call it again, the
	 * password file needs to be deleted.
	 * 
	 * @param password
	 * @throws IOException
	 * @throws SecurityException
	 *             if the password was already set
	 */
	public synchronized void setInitialEsAdminPassword(String password) throws IOException {
		if (getEntries().containsKey(SecurityConstants.ES_ADMIN)) {
			throw new SecurityException(
					SecurityConstants.ES_ADMIN + " user is already instantiated, please delete file or entry line");
		}

		addOrReplaceId(SecurityConstants.ES_ADMIN, Collections.singleton(SecurityConstants.ES_ADMIN), password);
	}

}
