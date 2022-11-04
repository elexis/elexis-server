package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContentComponent;
import org.hl7.fhir.r4.model.Enumerations.DocumentReferenceStatus;
import org.hl7.fhir.r4.model.Reference;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IDocumentStore;
import ch.elexis.core.utils.OsgiServiceUtil;
import info.elexis.server.fhir.rest.core.test.AllTests;
import info.elexis.server.fhir.rest.core.test.FhirUtil;

public class DocumentReferenceTest {

	private static IGenericClient client;

	private static IDocumentStore omnivoreDocumentStore;

	private static IDocumentStore letterDocumentStore;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		AllTests.getTestDatabaseInitializer().initializeBehandlung();

		client = FhirUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);

		omnivoreDocumentStore = OsgiServiceUtil
				.getService(IDocumentStore.class, "(storeid=ch.elexis.data.store.omnivore)").get();

		letterDocumentStore = OsgiServiceUtil
				.getService(IDocumentStore.class, "(storeid=ch.elexis.data.store.brief)").get();
	}

	@Test
	public void searchDocumentReference() throws ClientProtocolException, IOException, ElexisException {
		IPatient patient = AllTests.getTestDatabaseInitializer().getPatient();
		List<IDocument> existingDocuments = omnivoreDocumentStore.getDocuments(patient.getId(), null, null, null);
		Bundle results = client.search().forResource(DocumentReference.class)
				.where(DocumentReference.PATIENT.hasId(AllTests.getTestDatabaseInitializer().getPatient().getId()))
				.returnBundle(Bundle.class).execute();
		List<BundleEntryComponent> entries = results.getEntry();
		int existingEntriesSize = entries.size();
		assertEquals(existingDocuments.size(), existingEntriesSize);

		IDocument newDocument = omnivoreDocumentStore.createDocument(patient.getId(), "TestSearchDocumentReference.txt",
				null);
		omnivoreDocumentStore.saveDocument(newDocument, new ByteArrayInputStream("test content".getBytes()));
		results = client.search().forResource(DocumentReference.class)
				.where(DocumentReference.PATIENT.hasId(AllTests.getTestDatabaseInitializer().getPatient().getId()))
				.returnBundle(Bundle.class).execute();
		entries = results.getEntry();
		assertEquals(existingEntriesSize + 1, entries.size());

		newDocument = letterDocumentStore.createDocument(patient.getId(), "TestSearchDocumentReference 01.02.2022",
				null);
		newDocument.setMimeType("docx");
		letterDocumentStore.saveDocument(newDocument, new ByteArrayInputStream("test content".getBytes()));
		results = client.search().forResource(DocumentReference.class)
				.where(DocumentReference.PATIENT.hasId(AllTests.getTestDatabaseInitializer().getPatient().getId()))
				.returnBundle(Bundle.class).execute();
		entries = results.getEntry();
		Optional<DocumentReference> docx = entries.stream().filter(e -> e.getResource() instanceof DocumentReference)
				.map(e -> (DocumentReference) e.getResource())
				.filter(
						d -> d.getContentFirstRep().getAttachment().getTitle()
								.equals("TestSearchDocumentReference 01.02.2022.docx"))
				.findAny();
		assertTrue(docx.isPresent());
	}

	@Test
	public void getDocumentReference() throws ClientProtocolException, IOException {
		DocumentReference reference = new DocumentReference();
		reference.setStatus(DocumentReferenceStatus.CURRENT);
		reference.setSubject(
				(Reference) new Reference("Patient/" + AllTests.getTestDatabaseInitializer().getPatient().getId())
						.setId(AllTests.getTestDatabaseInitializer().getPatient().getId()));

		DocumentReferenceContentComponent content = new DocumentReferenceContentComponent();
		Attachment attachment = new Attachment();
		attachment.setTitle("test attachment.txt");
		content.setAttachment(attachment);
		reference.addContent(content);

		MethodOutcome outcome = client.create().resource(reference).execute();
		assertNotNull(outcome);
		assertTrue(outcome.getCreated());

		// upload content
		DocumentReference refWithContent = null;
		CloseableHttpClient ourHttpClient = HttpClients.createDefault();
		HttpPost post = new HttpPost(client.getServerBase() + "/" + ((DocumentReference) outcome.getResource()).getId()
				+ "/$binary-access-write");
		post.setEntity(
				new ByteArrayEntity("Test get Text\n2te Zeile üöä!".getBytes(), ContentType.APPLICATION_OCTET_STREAM));
		try (CloseableHttpResponse resp = ourHttpClient.execute(post)) {
			assertEquals(200, resp.getStatusLine().getStatusCode());

			String response = IOUtils.toString(resp.getEntity().getContent(), Constants.CHARSET_UTF8);
			System.out.println(response);
			refWithContent = client.getFhirContext().newJsonParser().parseResource(DocumentReference.class,
					response);
			byte[] actualBytes = readContent(refWithContent.getContentFirstRep().getAttachment());
			assertArrayEquals("Test get Text\n2te Zeile üöä!".getBytes(), actualBytes);
		}

		// search by patient
		Bundle results = client.search().forResource(DocumentReference.class)
				.where(DocumentReference.PATIENT.hasId(AllTests.getTestDatabaseInitializer().getPatient().getId()))
				.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());

	}

	@Test
	public void createDocumentReference() throws IOException {
		DocumentReference reference = new DocumentReference();
		reference.setStatus(DocumentReferenceStatus.CURRENT);
		reference.setSubject(
				(Reference) new Reference("Patient/" + AllTests.getTestDatabaseInitializer().getPatient().getId())
				.setId(AllTests.getTestDatabaseInitializer().getPatient().getId()));

		DocumentReferenceContentComponent content = new DocumentReferenceContentComponent();
		Attachment attachment = new Attachment();
		attachment.setTitle("test attachment.txt");
		attachment.setData(Base64.getEncoder().encode("Test Text\n2te Zeile üöä!".getBytes()));
		content.setAttachment(attachment);
		reference.addContent(content);

		MethodOutcome outcome = client.create().resource(reference).execute();
		assertNotNull(outcome);
		assertTrue(outcome.getCreated());
		assertNotNull(outcome.getId());

		DocumentReference readReference = client.read().resource(DocumentReference.class).withId(outcome.getId())
				.execute();
		assertNotNull(readReference);
		assertEquals(outcome.getId().getIdPart(), readReference.getIdElement().getIdPart());
		assertEquals(reference.getSubject().getReference(), readReference.getSubject().getReference());
		Attachment readAttachment = readReference.getContent().get(0).getAttachment();
		assertNotNull(readAttachment);
		byte[] actualBytes = readContent(readAttachment);
		assertArrayEquals("Test Text\n2te Zeile üöä!".getBytes(), actualBytes);

		outcome = client.update().resource(readReference).execute();
		assertFalse(outcome.getCreated() == null ? false : outcome.getCreated());
	}

	private byte[] readContent(Attachment attachment) throws IOException {
		byte[] ret = null;
		CloseableHttpClient ourHttpClient = HttpClients.createDefault();
		HttpGet get = new HttpGet(client.getServerBase() + "/" + attachment.getUrl());
		try (CloseableHttpResponse resp = ourHttpClient.execute(get)) {
			assertEquals(200, resp.getStatusLine().getStatusCode());
			ret = IOUtils.toByteArray(resp.getEntity().getContent());
		}
		return ret;
	}
}
