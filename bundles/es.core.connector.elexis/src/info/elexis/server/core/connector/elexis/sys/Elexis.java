package info.elexis.server.core.connector.elexis.sys;

import java.util.Collection;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Role;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.User;

public class Elexis {
	public static boolean userHasRole(User u, String role) {
		if (u == null || role == null) {
			throw new IllegalArgumentException();
		}
		Collection<Role> roles = u.getRoles();
		long count = roles.stream().filter(f -> role.equalsIgnoreCase(f.getId())).count();
		return (count > 0l);
	}
}
