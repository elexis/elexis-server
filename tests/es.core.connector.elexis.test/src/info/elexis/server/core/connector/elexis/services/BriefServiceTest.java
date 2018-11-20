//package info.elexis.server.core.connector.elexis.services;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertTrue;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.Optional;
//import java.util.Random;
//
//import org.exparity.hamcrest.date.LocalDateTimeMatchers;
//import org.hamcrest.MatcherAssert;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import ch.elexis.core.types.Gender;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Brief;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Heap;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
//
//public class BriefServiceTest {
//
//	Kontakt patient;
//
//	@Before
//	public void initialize() {
//		patient = new KontaktService.PersonBuilder("FirstName", "LastName", LocalDate.now(), Gender.MALE).patient()
//				.buildAndSave();
//	}
//
//	@After
//	public void cleanup() {
//		PersistenceService.remove(patient);
//	}
//
//	@Test
//	public void testCreateAndDeleteDocument() {
//		Brief document = new BriefService.Builder(patient).buildAndSave();
//		document.setSubject("TestSubject");
//		MatcherAssert.assertThat(document.getCreationDate(), LocalDateTimeMatchers.sameOrBefore(LocalDateTime.now()));
//
//		byte[] b = new byte[120];
//		new Random().nextBytes(b);
//		document.getContent().setInhalt(b);
//		BriefService.save(document);
//
//		document = BriefService.load(document.getId()).get();
//		assertNotNull(document.getContent());
//		assertEquals(document.getId(), document.getContent().getId());
//		assertEquals(patient.getId(), document.getPatient().getId());
//		assertNull(document.getGedruckt());
//
//		Optional<Heap> findById = HeapService.load(document.getId());
//		assertTrue(findById.isPresent());
//		assertEquals(findById.get().getId(), document.getContent().getId());
//		assertTrue(Arrays.equals(b, findById.get().getInhalt()));
//
//		BriefService.remove(document);
//
//		Optional<Heap> findDel = HeapService.load(document.getId());
//		assertFalse(findDel.isPresent());
//	}
//}
