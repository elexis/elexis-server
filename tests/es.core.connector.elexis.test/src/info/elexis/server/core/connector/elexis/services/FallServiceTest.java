package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.FallConstants;
import ch.elexis.core.types.Gender;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

public class FallServiceTest {

	Kontakt patient;
	Optional<Kontakt> patientO = Optional.empty();
	Fall fall;

	@Before
	public void initialize() {
		patient = new KontaktService.PersonBuilder("FirstName", "LastName", LocalDate.now(), Gender.MALE).patient()
				.buildAndSave();
	}

	@After
	public void cleanup() {
		FallService.remove(fall);
		PersistenceService.remove(patient);
	}

	@Test
	public void testCreateKontaktStringStringString() {
		fall = new FallService.Builder(patient, "test", FallConstants.TYPE_DISEASE, "UVG").buildAndSave();

		Fall storedFall = FallService.load(fall.getId()).get();
		assertEquals(fall.getPatientKontakt().getId(), storedFall.getPatientKontakt().getId());
		assertEquals(fall.getBezeichnung(), storedFall.getBezeichnung());
		assertEquals(fall.getGesetz(), storedFall.getGesetz());
		assertEquals("UVG", storedFall.getExtInfoAsString(FallConstants.FLD_EXTINFO_BILLING));

		List<Fall> faelle = KontaktService.getFaelle(patient);
		assertEquals(1, faelle.size());

		// test reference to patient
		assertEquals(0, patient.getFaelle().size());
		patient = KontaktService.reload(patient);
		assertEquals(1, patient.getFaelle().size());
		fall = new FallService.Builder(patient, "test1", FallConstants.TYPE_DISEASE, "UVG").buildAndSave();
		assertEquals(1, patient.getFaelle().size());
		patient = KontaktService.reload(patient);
		assertEquals(2, patient.getFaelle().size());
	}

	@Test
	public void testSetBillingAfterCreation() {
		fall = new FallService.Builder(patient, "description", FallConstants.TYPE_DISEASE, "UVG").buildAndSave();
		fall.setExtInfoValue(FallConstants.FLD_EXTINFO_BILLING, "insuranceType");
		FallService.save(fall);

		Fall storedFall = FallService.load(fall.getId()).get();
		assertEquals(patient.getId(), storedFall.getPatientKontakt().getId());
		assertEquals("description", storedFall.getBezeichnung());
		assertEquals("insuranceType", storedFall.getExtInfoAsString(FallConstants.FLD_EXTINFO_BILLING));
	}
}
