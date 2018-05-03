package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.User;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Userconfig;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;

public class UserconfigServiceTest {

	@BeforeClass
	public static void beforeClass() throws IOException, SQLException {
		new TestDatabaseInitializer().initializeMandant();
	}

	@Test
	public void testGet() {
		User user = UserService.load("tst").orElseThrow(() -> new IllegalStateException("No User"));
		assertNotNull(user);
		Kontakt kontakt = user.getKontakt();
		assertNotNull(kontakt);

		assertTrue(UserconfigService.get(kontakt, "NonExistingPrefDefaults", true));
		assertFalse(UserconfigService.get(kontakt, "NonExistingPrefDefaults", false));

		Userconfig userConfig = new UserconfigService.Builder(kontakt, "ExistingPref", "test").buildAndSave();

		kontakt = KontaktService.reload(kontakt);
		assertEquals(1, kontakt.getUserconfig().size());

		assertEquals(userConfig.getValue(), kontakt.getUserconfig().get(0).getValue());
		assertEquals(userConfig.getValue(), UserconfigService.get(kontakt, "ExistingPref", (String) null));
	}
	
	@Test
	public void testFindAllEntries() {
		List<Userconfig> findAllEntries = UserconfigService.findAllEntries();
		assertEquals(1, findAllEntries.size());
	}
}
