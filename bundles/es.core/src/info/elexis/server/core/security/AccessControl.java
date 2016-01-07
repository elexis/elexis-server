package info.elexis.server.core.security;

import java.security.Principal;

public class AccessControl {

	public static boolean request(Principal user, String role) {
		if(user.equals(User.ADMIN)) return true;
		
		return false;
	}

}
