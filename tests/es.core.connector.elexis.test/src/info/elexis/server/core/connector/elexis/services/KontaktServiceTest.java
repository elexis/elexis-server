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
	public void testCreateAndDeleteKontakt() throws InstantiationException, IllegalAccessException {
		Kontakt val = KontaktService.INSTANCE.create();
		Kontakt findById = KontaktService.INSTANCE.findById(val.getId()).get();
		assertEquals(val.getId(), findById.getId());
		KontaktService.INSTANCE.remove(val);
		Optional<Kontakt> found = KontaktService.INSTANCE.findById(val.getId());
		assertFalse(found.isPresent());
	}

	@Test
	public void testFindByIdStartingWith() {
		Kontakt val = KontaktService.INSTANCE.create();
		List<Kontakt> result = KontaktService.INSTANCE.findByIdStartingWith(val.getId().substring(0, 5));
		assertEquals(1, result.size());
		KontaktService.INSTANCE.remove(val);
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
		Kontakt val = KontaktService.INSTANCE.create();

		System.out.println(val.getId());

		val.setDescription1(
				"ThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255CharactersThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255CharactersThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255CharactersThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255CharactersThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255Characters");

		KontaktService.INSTANCE.flush();

		KontaktService.INSTANCE.remove(val);

	}

	@Test
	public void testCreatePatient() {
		Kontakt patient = KontaktService.INSTANCE.createPatient("Vorname", "Nachname", LocalDate.now(), Gender.FEMALE);
		patient.setExtInfoValue(PatientConstants.FLD_EXTINFO_BIRTHNAME, "Birthname");
		String id = patient.getId();

		assertNotNull(id);
		assertNotNull(patient.getCode());
		Kontakt findById = KontaktService.INSTANCE.findById(id).get();
		assertNotNull(findById);
		assertTrue(findById == patient);

		assertEquals("Birthname", findById.getExtInfoAsString(PatientConstants.FLD_EXTINFO_BIRTHNAME));

		KontaktService.INSTANCE.remove(patient);
	}

	@Test
	public void testGetAgeInYears() {
		Optional<Kontakt> male = KontaktService.INSTANCE.findById(TestEntities.PATIENT_MALE_ID);
		int ageInYears = KontaktService.getAgeInYears(male.get());
		assertEquals(37, ageInYears);
	}

	@Test
	public void testFindPatientByPatientNumber() {
		Optional<Kontakt> patient = KontaktService.findPatientByPatientNumber(TestEntities.PATIENT_MALE_PATIENTNR);
		assertTrue(patient.isPresent());
		Optional<Kontakt> findById = KontaktService.INSTANCE.findById(TestEntities.PATIENT_MALE_ID);
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
		Optional<Kontakt> findById = KontaktService.INSTANCE.findById(TestEntities.MANDATOR_ID);
		assertTrue(findById.isPresent());
		assertEquals("Nachname Hauptanwender", findById.get().getExtInfoAsString("Mandant"));
		assertEquals(ContactType.PERSON, findById.get().getContactType());
	}

	@Test
	public void testFindLaboratory() {
		Optional<IContact> lab = KontaktService.findLaboratory(TestEntities.LABORATORY_IDENTIFIER);
		assertTrue(lab.isPresent());
		Optional<Kontakt> findById = KontaktService.INSTANCE.findById(TestEntities.LABORATORY_ID);
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
