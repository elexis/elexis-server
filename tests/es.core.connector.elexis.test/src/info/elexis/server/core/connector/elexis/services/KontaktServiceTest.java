package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import ch.elexis.core.model.PatientConstants;
import ch.elexis.core.types.Gender;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

public class KontaktServiceTest {

	@Test
	public void testCreateAndDeleteKontakt() throws InstantiationException, IllegalAccessException {
		Kontakt val = KontaktService.INSTANCE.create();
		Kontakt findById = KontaktService.INSTANCE.findById(val.getId());
		assertEquals(val.getId(), findById.getId());
		KontaktService.INSTANCE.remove(val);	
		Kontakt found = KontaktService.INSTANCE.findById(val.getId());
		assertNull(found);
	}
	
	@Test
	public void testFindByIdStartingWith()  {
		Kontakt val = KontaktService.INSTANCE.create();
		List<Kontakt> result = KontaktService.INSTANCE.findByIdStartingWith(val.getId().substring(0, 1));
		assertEquals(1, result.size());
		KontaktService.INSTANCE.remove(val);
	}
	
	@Test
	public void testCreateAndDeletePatient() {
		Kontakt patient = KontaktService.INSTANCE.createPatient("Vorname", "Nachname", LocalDate.now(), Gender.FEMALE);
		patient.getExtInfo().put(PatientConstants.FLD_EXTINFO_BIRTHNAME, "Birthname");
		String id = patient.getId();
		
		assertNotNull(id);
		assertNotNull(patient.getCode());
		Kontakt findById = KontaktService.INSTANCE.findById(id);
		assertNotNull(findById);
		assertTrue(findById == patient);

		assertEquals("Birthname", patient.getExtInfo().get(PatientConstants.FLD_EXTINFO_BIRTHNAME));
		
		KontaktService.INSTANCE.remove(patient);
	}
}
