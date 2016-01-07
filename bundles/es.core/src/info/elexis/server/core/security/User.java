package info.elexis.server.core.security;

import java.security.Principal;

public class User implements Principal {

	public static final User ADMIN = new User("admin");
	
	private final String name;

	public User(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}
