package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.types.Gender;
import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Brief;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Heap;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

public class BriefServiceTest {


	Kontakt patient;

	@Before
	public void initialize() {
		patient = KontaktService.INSTANCE.createPatient("FirstName", "LastName", LocalDate.now(), Gender.MALE);
	}
	
	@After
	public void cleanup() {
		KontaktService.INSTANCE.remove(patient);
	}

	@Test
	public void testCreateAndDeleteDocument() {
		Brief document = BriefService.INSTANCE.create();
		document.setPatient(patient);
		document.setSubject("TestSubject");
		BriefService.INSTANCE.flush();

		EntityManager em = ElexisEntityManager.createEntityManager();
		Brief storedDocument = em.find(Brief.class, document.getId());
		assertEquals(patient.getId(), storedDocument.getPatient().getId());
		assertEquals("TestSubject", storedDocument.getSubject());
		Optional<Heap> findById = HeapService.INSTANCE.findById(document.getId());
		assertTrue(findById.isPresent());
		em.close();
		
		BriefService.INSTANCE.remove(document);
		HeapService.INSTANCE.flush();
		
		Optional<Heap> findDel = HeapService.INSTANCE.findById(document.getId());
		assertFalse(findDel.isPresent());		
	}
}
