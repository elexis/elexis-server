package info.elexis.server.core.security;

import java.io.IOException;
import java.util.Set;

import info.elexis.server.core.internal.security.ElexisServerAuthenticationFile;

/**
 * Management of elexis-server system local users
 */
public class LocalUserUtil {

	public static void addOrReplaceLocalUser(String username, String password, Set<String> roles) throws IOException {
		ElexisServerAuthenticationFile.getInstance().addOrReplaceId(username, roles, password);
	}

}
