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
		patient = new KontaktService.PersonBuilder("FirstName", "LastName", LocalDate.now(), Gender.MALE).patient()
				.buildAndSave();
	}

	@After
	public void cleanup() {
		PersistenceService.remove(patient);
	}

	@Test
	public void testCreateAndDeleteDocument() {
		Brief document = new BriefService.Builder(patient).buildAndSave();
		document.setSubject("TestSubject");
		Heap content = document.getContent();
		assertNotNull(content);
		BriefService.save(content);

		HeapService.remove(document.getContent());
		BriefService.remove(document);

		Optional<Heap> findDel = HeapService.load(document.getId());
		assertFalse(findDel.isPresent());
	}

	@Test
	public void testLoadAndModifyDocument() {
		Brief document = new BriefService.Builder(patient).buildAndSave();

		EntityManager em = ElexisEntityManager.createEntityManager();
		Brief storedDocument = em.find(Brief.class, document.getId());
		em.close();
		assertEquals(patient.getId(), storedDocument.getPatient().getId());
		assertNotNull(storedDocument.getContent());
		assertEquals(document.getId(), storedDocument.getContent().getId());
		Optional<Heap> findById = HeapService.load(document.getId());
		assertTrue(findById.isPresent());
		assertEquals(findById.get().getId(), storedDocument.getContent().getId());

		HeapService.remove(document.getContent());
		BriefService.remove(document);
	}
}
