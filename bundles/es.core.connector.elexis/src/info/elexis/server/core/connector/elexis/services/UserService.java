package info.elexis.server.core.connector.elexis.services;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.User;

public class UserService extends AbstractService<User> {

	public UserService() {
		super(User.class);
	}
}
