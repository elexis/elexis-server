package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.elexis.core.constants.Preferences;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.User;

public class UserconfigServiceTest {

	@Test
	public void testGet() {
		User dzUser = UserService.INSTANCE.findById("dz").get();
		assertNotNull(dzUser);
		Kontakt kontakt = dzUser.getKontakt();
		assertNotNull(kontakt);
		
		UserconfigService.INSTANCE.get(kontakt, Preferences.LEISTUNGSCODES_OPTIFY, true);
		boolean b = UserconfigService.INSTANCE.get(kontakt, "NonExistingPrefDefaults", false);
		assertFalse(b);
	}
	
	

}
