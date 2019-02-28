package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.exparity.hamcrest.date.LocalDateTimeMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.types.Gender;
import info.elexis.server.core.common.LocalProperties;
import info.elexis.server.core.connector.elexis.Properties;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Brief;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Heap;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.test.TestEntities;

public class BriefServiceTest {

	private static Path tempDir;
	
	Kontakt patient;
	
	@BeforeClass
	public static void init() {
		try {
			tempDir = Files.createTempDirectory(null);
			tempDir.toFile().deleteOnExit();
		} catch (IOException e) {
			fail("Failed initialize temp directory:" + e.getMessage());
		}
		
		LocalProperties.setProperty(Properties.PROPERTY_BRIEFE_NETWORK_PATH, tempDir.toFile().getAbsolutePath());
	}

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
		ConfigService.INSTANCE.setFromBoolean(Preferences.P_TEXT_EXTERN_FILE, false);
		
		Brief document = new BriefService.Builder(patient).buildAndSave();
		document.setSubject("TestSubject");
		MatcherAssert.assertThat(document.getCreationDate(), LocalDateTimeMatchers.sameOrBefore(LocalDateTime.now()));

		byte[] b = new byte[120];
		new Random().nextBytes(b);
		document.setContent(b);
		BriefService.save(document);

		document = BriefService.load(document.getId()).get();
		assertNotNull(document.getContent());
		assertEquals(patient.getId(), document.getPatient().getId());
		assertNull(document.getGedruckt());

		Optional<Heap> findById = HeapService.load(document.getId());
		assertTrue(Arrays.equals(b, findById.get().getInhalt()));

		BriefService.remove(document);

		Optional<Heap> findDel = HeapService.load(document.getId());
		assertFalse(findDel.isPresent());
	}
	
	@Test
	public void briefGetContentConsideringNetworkPathStoreIfRequired_SaveFile() {
		ConfigService.INSTANCE.setFromBoolean(Preferences.P_TEXT_EXTERN_FILE, true);
		ConfigService.INSTANCE.set(Preferences.P_TEXT_EXTERN_FILE_PATH, tempDir.toFile().getAbsolutePath());

		Optional<Kontakt> patient = KontaktService.findPatientByPatientNumber(TestEntities.PATIENT_MALE_PATIENTNR);
		byte[] sampleDocument = new byte[512];
		ThreadLocalRandom.current().nextBytes(sampleDocument);
		
		Brief brief = new BriefService.Builder(patient.get()).build();
		brief.setSubject("TestSubject2");
		brief.setMimetype("docx");
		brief.setContent(sampleDocument);
		BriefService.save(brief);
		
		Path path = Paths.get(patient.get().getPatientNr(), brief.getId() + ".docx");
		Path createdFile = tempDir.resolve(path);
		assertTrue(createdFile.toFile().exists() && createdFile.toFile().canRead());
		
		Optional<Heap> heap = HeapService.load(brief.getId());
		assertFalse(heap.isPresent());

		byte[] result = BriefService.load(brief.getId()).get().getContent();
		assertTrue(Arrays.equals(sampleDocument, result));
		
		BriefService.remove(brief);
	}
	
	@Test
	public void briefGetContentConsideringNetworkPathStoreIfRequired_SaveHeap() {
		ConfigService.INSTANCE.setFromBoolean(Preferences.P_TEXT_EXTERN_FILE, false);
	
		Optional<Kontakt> patient = KontaktService.findPatientByPatientNumber(TestEntities.PATIENT_MALE_PATIENTNR);
		byte[] sampleDocument = new byte[512];
		ThreadLocalRandom.current().nextBytes(sampleDocument);
		
		Brief brief = new BriefService.Builder(patient.get()).build();
		brief.setSubject("TestSubject");
		brief.setMimetype("docx");
		brief.setContent(sampleDocument);
		BriefService.save(brief);
		
		Path path = Paths.get(patient.get().getPatientNr(), brief.getId() + ".docx");
		Path createdFile = tempDir.resolve(path);
		assertFalse(createdFile.toFile().exists());

		Optional<Heap> heap = HeapService.load(brief.getId());
		assertTrue(heap.isPresent());
		assertTrue(Arrays.equals(sampleDocument, heap.get().getInhalt()));
		
		byte[] result = BriefService.load(brief.getId()).get().getContent();
		assertTrue(Arrays.equals(sampleDocument, result));
		
		BriefService.remove(brief);
	}

}
