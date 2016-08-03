package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import ch.elexis.core.model.PatientConstants;
import ch.elexis.core.types.Gender;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

public class KontaktServiceTest {

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
	public void testCreateAndDeletePatient() {
		Kontakt patient = KontaktService.INSTANCE.createPatient("Vorname", "Nachname", LocalDate.now(), Gender.FEMALE);
		patient.setExtInfoValue(PatientConstants.FLD_EXTINFO_BIRTHNAME, "Birthname");
		String id = patient.getId();

		assertNotNull(id);
		assertNotNull(patient.getCode());
		Kontakt findById = KontaktService.INSTANCE.findById(id).get();
		assertNotNull(findById);
		assertTrue(findById == patient);

		assertEquals("Birthname", patient.getExtInfoAsString(PatientConstants.FLD_EXTINFO_BIRTHNAME));

		KontaktService.INSTANCE.remove(patient);
	}
	
	@Test
	public void testCreateAndDeleteKontaktWithStringExceedsLimit()
			throws InstantiationException, IllegalAccessException {
		Kontakt val = KontaktService.INSTANCE.create();

		System.out.println(val.getId());
		
		val.setDescription1(
				"ThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255CharactersThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255CharactersThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255CharactersThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255CharactersThisIsAVeryLongStringWhichsOnlyPurposeIsToExceedTheBoundaryOf255Characters");

		KontaktService.INSTANCE.flush();
		
		KontaktService.INSTANCE.remove(val);

	}
}
