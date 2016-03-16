package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.util.Optional;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.FallConstants;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

public class FallServiceTest {

	Kontakt patient;
	Optional<Kontakt> patientO = Optional.empty();
	Fall fall;

	@Before
	public void initialize() {
		int randomPatientNumber = -1;
		while (!patientO.isPresent()) {
			randomPatientNumber = new Random().nextInt(500) + 100;
			System.out.println("Trying to find random patient " + randomPatientNumber);
			patientO = KontaktService.findPatientByPatientNumber(randomPatientNumber);
		}
		patient = patientO.get();
	}
	
	@After
	public void cleanup() {
		FallService.INSTANCE.remove(fall);
	}

	@Test
	public void testCreateKontaktStringStringString() {
		fall = FallService.INSTANCE.create(patient, "test", FallConstants.TYPE_DISEASE, "UVG");

		Fall storedFall = FallService.INSTANCE.findById(fall.getId());
		assertEquals(fall.getPatientKontakt().getId(), storedFall.getPatientKontakt().getId());
		assertEquals(fall.getBezeichnung(), storedFall.getBezeichnung());
		assertEquals(fall.getGesetz(), storedFall.getGesetz());
		assertEquals("UVG", storedFall.getExtInfo().get(FallConstants.FLD_EXTINFO_BILLING));
	}
}
