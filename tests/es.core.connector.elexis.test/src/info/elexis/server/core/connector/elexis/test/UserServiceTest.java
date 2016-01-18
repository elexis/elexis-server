package info.elexis.server.core.connector.elexis.test;

import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.User;
import info.elexis.server.core.connector.elexis.services.UserService;

public class UserServiceTest {

	@Test
	public void testFindAll() {
		UserService us = new UserService();
		List<User> findAll = us.findAll(true);
		assertTrue((findAll.size()>2));
	}

}
