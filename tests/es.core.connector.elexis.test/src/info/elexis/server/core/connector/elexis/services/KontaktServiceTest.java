package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.PatientConstants;
import ch.elexis.core.types.ContactType;
import ch.elexis.core.types.Gender;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.test.TestEntities;

public class KontaktServiceTest extends AbstractServiceTest {

	@Test
	public void testCreateAndRemoveKontakt() throws InstantiationException, IllegalAccessException {
		Kontakt val = new KontaktService.PersonBuilder("", "", LocalDate.of(2016, 12, 12), Gender.MALE).buildAndSave();
		assertNotNull(val.getId());
		assertNotNull(val.getLastupdate());
		Kontakt findById = KontaktService.load(val.getId()).get();
		assertEquals(val.getId(), findById.getId());
		KontaktService.remove(val);
		Optional<Kontakt> found = KontaktService.load(val.getId());
		assertFalse(found.isPresent());
	}

	@Test
	public void testFindAllPatients() {
		List<Kontakt> findAllPatients = KontaktService.findAllPatients();
		assertTrue(findAllPatients.size() > 0);
	}

	@Test
	@Ignore
	public void testCreateAndDeleteKontaktWithStringExceedsLimit()
			throws InstantiationException, IllegalAccessException {
		Kontakt val = new KontaktService.PersonBuilder("", "", LocalDate.of(2016, 12, 12), Gender.MALE).buildAndSave();

		val.setDescription1(
				"ThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255CharactersThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255CharactersThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255CharactersThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255CharactersThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255Characters");

		KontaktService.save(val);
	}

	@Test
	public void testCreatePatient() {
		Kontakt patient = new KontaktService.PersonBuilder("Vorname", "Nachname", LocalDate.now(), Gender.FEMALE)
				.patient().buildAndSave();
		patient.setExtInfoValue(PatientConstants.FLD_EXTINFO_BIRTHNAME, "Birthname");
		KontaktService.save(patient);
		String id = patient.getId();

		assertNotNull(id);
		assertNotNull(patient.getCode());
		Kontakt findById = KontaktService.load(id).get();
		assertNotNull(findById);
		assertEquals(findById, patient);
		assertEquals("Birthname", findById.getExtInfoAsString(PatientConstants.FLD_EXTINFO_BIRTHNAME));

		KontaktService.remove(patient);
	}

	@Test
	public void testGetAgeInYears() {
		Optional<Kontakt> male = KontaktService.load(TestEntities.PATIENT_MALE_ID);
		int ageInYears = KontaktService.getAgeInYears(male.get());
		assertEquals(37, ageInYears);
	}

	@Test
	public void testFindPatientByPatientNumber() {
		Optional<Kontakt> patient = KontaktService.findPatientByPatientNumber(TestEntities.PATIENT_MALE_PATIENTNR);
		assertTrue(patient.isPresent());
		Optional<Kontakt> findById = KontaktService.load(TestEntities.PATIENT_MALE_ID);
		assertTrue(findById.isPresent());
		assertEquals(findById.get().getId(), patient.get().getId());

		assertEquals("Testpatient", patient.get().getDescription1());
		assertEquals("Vorname", patient.get().getDescription2());
		assertEquals(new TimeTool(LocalDate.of(1979, 3, 15)), patient.get().getDateOfBirth());
		assertEquals(Gender.MALE, patient.get().getGender());
		assertEquals(ContactType.PATIENT, patient.get().getContactType());
		assertEquals("Teststrasse 15", patient.get().getStreet());
		assertEquals("Testort", patient.get().getCity());
		assertEquals("6840", patient.get().getZip());
	}

	@Test
	public void testLoadMandatorAndExtInfoValues() {
		Optional<Kontakt> findById = KontaktService.load(TestEntities.MANDATOR_ID);
		assertTrue(findById.isPresent());
		assertEquals("Nachname Hauptanwender", findById.get().getExtInfoAsString("Mandant"));
		assertEquals(ContactType.PERSON, findById.get().getContactType());
	}

	@Test
	public void testFindLaboratory() {
		Optional<IContact> lab = KontaktService.findLaboratory(TestEntities.LABORATORY_IDENTIFIER);
		assertTrue(lab.isPresent());
		Optional<Kontakt> findById = KontaktService.load(TestEntities.LABORATORY_ID);
		assertTrue(findById.isPresent());
		assertEquals(findById.get().getId(), lab.get().getId());
		assertEquals(ContactType.LABORATORY, lab.get().getContactType());
	}

	@Test
	@Ignore
	public void testFindPersonByMultipleOptionalParameters() {
		fail("Not yet implemented");
	}
}
