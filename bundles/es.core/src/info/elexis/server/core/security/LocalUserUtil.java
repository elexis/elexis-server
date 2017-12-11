package info.elexis.server.core.security;

import java.io.IOException;
import java.util.Set;

import info.elexis.server.core.security.internal.ElexisServerAuthenticationFile;

/**
 * Management of elexis-server system local users
 */
public class LocalUserUtil {

	public static String addOrReplaceLocalUser(String userId, String password, Set<String> roles) throws IOException {
		return ElexisServerAuthenticationFile.getInstance().addOrReplaceId(userId, roles, password);
	}

	public static void removeLocalUser(String userId) throws IOException {
		ElexisServerAuthenticationFile.getInstance().removeId(userId);

	}

	public static String printLocalUsers() {
		return ElexisServerAuthenticationFile.getInstance().printEntries();
	}

}
