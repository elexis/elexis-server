package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.BeforeClass;
import org.junit.Test;

import info.elexis.server.core.common.LocalProperties;
import info.elexis.server.core.connector.elexis.Properties;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.DocHandle;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.test.TestEntities;

public class DocHandleServiceTest extends AbstractServiceTest {

	private static Path tempDir;

	@BeforeClass
	public static void init() {
		try {
			tempDir = Files.createTempDirectory(null);
			tempDir.toFile().deleteOnExit();
		} catch (IOException e) {
			fail("Failed initialize temp directory:" + e.getMessage());
		}
	}

	@Test
	public void testCreateKontaktStringStringStringByteArray() {
		ConfigService.INSTANCE.setFromBoolean(DocHandleService.CONFIG_OMNIVORE_STORE_GLOBAL, false);
		ConfigService.INSTANCE.setFromBoolean(DocHandleService.CONFIG_OMNIVORE_STORE_IN_FS, false);

		Optional<Kontakt> patient = KontaktService.findPatientByPatientNumber(TestEntities.PATIENT_MALE_PATIENTNR);
		assertTrue(patient.isPresent());
		byte[] sampleDocument = new byte[512];
		ThreadLocalRandom.current().nextBytes(sampleDocument);

		DocHandle docHandle = DocHandleService.INSTANCE.create(patient.get(), "Title", "Filename.pdf", "Category",
				sampleDocument);
		LocalDate now = LocalDate.now();
		docHandle.setCreationDate(now);
		DocHandleService.INSTANCE.flush();

		Optional<DocHandle> findById = DocHandleService.INSTANCE.findById(docHandle.getId());
		assertTrue(findById.isPresent());
		assertEquals("Title", findById.get().getTitle());
		assertEquals("Filename.pdf", findById.get().getMimetype());
		assertEquals("Category", findById.get().getCategory());
		assertEquals(now.toEpochDay(), findById.get().getDatum().toEpochDay());
		assertEquals(now.toEpochDay(), findById.get().getCreationDate().toEpochDay());
		assertTrue(Arrays.equals(sampleDocument, findById.get().getDoc()));
	}

	@Test
	public void testGetAllCategories() {
		List<DocHandle> categories = DocHandleService.omnivoreGetAllCategories();
		assertTrue(categories.size() > 0);
	}

	@Test
	public void testOmnivoreGetContentConsideringNetworkPathStoreIfRequired() {
		ConfigService.INSTANCE.setFromBoolean(DocHandleService.CONFIG_OMNIVORE_STORE_GLOBAL, true);
		ConfigService.INSTANCE.setFromBoolean(DocHandleService.CONFIG_OMNIVORE_STORE_IN_FS, true);

		LocalProperties.setProperty(Properties.PROPERTY_OMNIVORE_NETWORK_PATH, tempDir.toFile().getAbsolutePath());

		Optional<Kontakt> patient = KontaktService.findPatientByPatientNumber(TestEntities.PATIENT_MALE_PATIENTNR);
		byte[] sampleDocument = new byte[512];
		ThreadLocalRandom.current().nextBytes(sampleDocument);

		DocHandle docHandle = DocHandleService.INSTANCE.create(patient.get(), "Title", "Filename.pdf", "Category",
				sampleDocument);
		Optional<DocHandle> findById = DocHandleService.INSTANCE.findById(docHandle.getId());

		Path path = Paths.get(patient.get().getPatientNr(), findById.get().getId() + ".pdf");
		Path createdFile = tempDir.resolve(path);
		assertTrue(createdFile.toFile().exists() && createdFile.toFile().canRead());

		byte[] result = DocHandleService.omnivoreGetContentConsideringNetworkPathStoreIfRequired(findById.get());
		assertTrue(Arrays.equals(sampleDocument, result));
	}

	@Test
	public void testOmnivoreStoreContentConsideringNetworkPathStoreIfRequired() throws IOException {
		ConfigService.INSTANCE.setFromBoolean(DocHandleService.CONFIG_OMNIVORE_STORE_GLOBAL, true);
		ConfigService.INSTANCE.setFromBoolean(DocHandleService.CONFIG_OMNIVORE_STORE_IN_FS, true);

		LocalProperties.setProperty(Properties.PROPERTY_OMNIVORE_NETWORK_PATH, tempDir.toFile().getAbsolutePath());

		Optional<Kontakt> patient = KontaktService.findPatientByPatientNumber(TestEntities.PATIENT_MALE_PATIENTNR);
		byte[] sampleDocument = new byte[512];
		ThreadLocalRandom.current().nextBytes(sampleDocument);

		DocHandle docHandle = DocHandleService.INSTANCE.create(patient.get(), "Title", "Filename.pdf", "Category",
				sampleDocument);

		Optional<DocHandle> findById = DocHandleService.INSTANCE.findById(docHandle.getId());
		assertTrue(findById.isPresent());
		assertEquals("Title", findById.get().getTitle());
		assertEquals("Filename.pdf", findById.get().getMimetype());
		assertEquals("Category", findById.get().getCategory());

		Path path = Paths.get(patient.get().getPatientNr(), findById.get().getId() + ".pdf");
		Path createdFile = tempDir.resolve(path);
		assertTrue(createdFile.toFile().exists() && createdFile.toFile().canRead());
		byte[] storedValue = Files.readAllBytes(createdFile);
		assertTrue(Arrays.equals(sampleDocument, storedValue));
	}

	@Test
	public void testdetermineByteArrayLength() throws IOException {
		ConfigService.INSTANCE.setFromBoolean(DocHandleService.CONFIG_OMNIVORE_STORE_GLOBAL, false);
		ConfigService.INSTANCE.setFromBoolean(DocHandleService.CONFIG_OMNIVORE_STORE_IN_FS, false);

		LocalProperties.setProperty(Properties.PROPERTY_OMNIVORE_NETWORK_PATH, tempDir.toFile().getAbsolutePath());

		Optional<Kontakt> patient = KontaktService.findPatientByPatientNumber(TestEntities.PATIENT_MALE_PATIENTNR);
		assertTrue(patient.isPresent());
		byte[] sampleDocument = new byte[32835];
		ThreadLocalRandom.current().nextBytes(sampleDocument);

		DocHandle docHandle = DocHandleService.INSTANCE.create(patient.get(), "Title2", "Filename2.pdf", "Category",
				sampleDocument);

		long determineByteArrayLength = DocHandleService.INSTANCE.determineByteArrayLength(docHandle);
		assertEquals(32835, determineByteArrayLength);

		ConfigService.INSTANCE.setFromBoolean(DocHandleService.CONFIG_OMNIVORE_STORE_GLOBAL, true);
		ConfigService.INSTANCE.setFromBoolean(DocHandleService.CONFIG_OMNIVORE_STORE_IN_FS, true);

		LocalProperties.setProperty(Properties.PROPERTY_OMNIVORE_NETWORK_PATH, tempDir.toFile().getAbsolutePath());

		byte[] sampleDocument2 = new byte[234523];
		ThreadLocalRandom.current().nextBytes(sampleDocument2);

		DocHandle docHandle2 = DocHandleService.INSTANCE.create(patient.get(), "Title3", "Filename.pdf3", "Category",
				sampleDocument2);
		long determineByteArrayLength2 = DocHandleService.INSTANCE.determineByteArrayLength(docHandle2);
		assertEquals(234523, determineByteArrayLength2);
	}

}
