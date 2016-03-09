package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import ch.elexis.core.model.PatientConstants;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Xid;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.types.Gender;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.types.XidQuality;

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
		 List<Kontakt> result = KontaktService.INSTANCE.findByIdStartingWith("A");
		 assertTrue("Found more than 2 contacts", result.size()>2);
	}
	
	@Test
	public void testFindByIdAndListFaelle() {
		Kontakt cont = KontaktService.INSTANCE.findById("ab692057d60c01b62016460");
		Xid xid = XidService.INSTANCE.create("blaDomain", "blaDomainId", cont, XidQuality.ASSIGNMENT_GLOBAL);
		cont = KontaktService.INSTANCE.findById("ab692057d60c01b62016460");
		List<Fall> faelle = cont.getFaelle();
		assertTrue("Found more than one fall", faelle.size()>1);
		Set<String> xids = cont.getXids().keySet();
		assertTrue("Found at least one Xid", xids.size()>0);
	}
	
	public void testCreateAndDeletePatient() {
		Kontakt patient = KontaktService.INSTANCE.createPatient("Vorname", "Nachname", LocalDate.now(), Gender.FEMALE);
		patient.getExtInfo().put(PatientConstants.FLD_EXTINFO_BIRTHNAME, "Birthname");
		String id = patient.getId();
		KontaktService.INSTANCE.flush();
		
		assertNotNull(id);
		assertNotNull(patient.getPatientNr());

		Kontakt findById = KontaktService.INSTANCE.findById(id);
		assertNotNull(findById);
		
		assertEquals("Birthname", patient.getExtInfo().get(PatientConstants.FLD_EXTINFO_BIRTHNAME));
	}
}
