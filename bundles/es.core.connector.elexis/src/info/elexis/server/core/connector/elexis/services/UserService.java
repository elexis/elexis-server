package info.elexis.server.core.connector.elexis.services;

import java.util.Collection;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Role;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.User;

public class UserService extends AbstractService<User> {

	public static UserService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final UserService INSTANCE = new UserService();
	}

	private UserService() {
		super(User.class);
	}

	public boolean userHasRole(User u, String role) {
		if (u == null || role == null) {
			throw new IllegalArgumentException();
		}
		Collection<Role> roles = u.getRoles();
		long count = roles.stream().filter(f -> role.equalsIgnoreCase(f.getId())).count();
		return (count > 0l);
	}
}
