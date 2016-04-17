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
		HeapService.INSTANCE.flush();
		HeapService.INSTANCE.flush();
	}

	@Test
	public void testCreateAndDeleteDocument() {
		Brief document = BriefService.INSTANCE.create(patient);
		document.setSubject("TestSubject");
		Heap content = document.getContent();
		assertNotNull(content);
		BriefService.INSTANCE.flush();

		HeapService.INSTANCE.remove(document.getContent());
		BriefService.INSTANCE.remove(document);

		Optional<Heap> findDel = HeapService.INSTANCE.findById(document.getId());
		assertFalse(findDel.isPresent());
	}

	@Test
	public void testLoadAndModifyDocument() {
		Brief document = BriefService.INSTANCE.create(patient);
		EntityManager em = ElexisEntityManager.createEntityManager();
		Brief storedDocument = em.find(Brief.class, document.getId());
		assertEquals(patient.getId(), storedDocument.getPatient().getId());
		assertNotNull(storedDocument.getContent());
		assertEquals(document.getId(), storedDocument.getContent().getId());
		Optional<Heap> findById = HeapService.INSTANCE.findById(document.getId());
		assertTrue(findById.isPresent());
		em.close();

		HeapService.INSTANCE.remove(document.getContent());
		BriefService.INSTANCE.remove(document);
	}
}
