package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Test;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.User;

public class UserServiceTest {

	@Test
	public void testLoadAdministratorUser() {
		Optional<User> admin = UserService.load("Administrator");
		assertTrue(admin.isPresent());
	}

}
