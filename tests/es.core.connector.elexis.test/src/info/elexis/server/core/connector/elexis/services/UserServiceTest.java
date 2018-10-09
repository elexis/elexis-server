//package info.elexis.server.core.connector.elexis.services;
//
//import static org.junit.Assert.*;
//
//import java.time.LocalDate;
//import java.util.Collection;
//import java.util.Optional;
//
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import ch.elexis.core.model.RoleConstants;
//import ch.elexis.core.types.Gender;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Role;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.User;
//
//public class UserServiceTest {
//
//	static String usernameId = "TestUser";
//	static Kontakt mandant;
//
//	@BeforeClass
//	public static void beforeClass() {
//		String cts = String.valueOf(System.currentTimeMillis());
//
//		mandant = new KontaktService.PersonBuilder(usernameId, cts, LocalDate.now(), Gender.MALE).buildAndSave();
//		new UserService.Builder(usernameId, mandant).buildAndSave();
//	}
//
//	@Test
//	public void testLoadAdministratorUser() {
//		Optional<User> admin = UserService.load("Administrator");
//		assertTrue(admin.isPresent());
//	}
//
//	@Test
//	public void testCreateUserVerifyPassword() {
//		Optional<User> load = UserService.load(usernameId);
//		assertTrue(load.isPresent());
//		assertEquals(usernameId, load.get().getId());
//		assertEquals(mandant, load.get().getKontakt());
//		assertNotNull(load.get().getHashedPassword());
//		assertNotNull(load.get().getSalt());
//		Collection<Role> roles = load.get().getRoles();
//		assertNotNull(roles);
//		assertEquals(RoleConstants.SYSTEMROLE_LITERAL_USER, roles.iterator().next().getId());
//
//		assertFalse(UserService.verifyPassword(load.get(), "invalid"));
//
//		UserService.setPasswordForUser(load.get(), "password");
//		assertTrue(UserService.verifyPassword(load.get(), "password"));
//
//		assertTrue(UserService.userHasRole(load.get(), RoleConstants.SYSTEMROLE_LITERAL_USER));
//	}
//
//	@Test
//	public void testFindByContact() {
//		Optional<User> findByKontakt = UserService.findByKontakt(mandant);
//		assertTrue(findByKontakt.isPresent());
//		assertEquals(usernameId, findByKontakt.get().getId());
//	}
//
//}
