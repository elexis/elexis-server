package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContentComponent;
import org.hl7.fhir.r4.model.Enumerations.DocumentReferenceStatus;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Reference;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.util.CodeTypeUtil;
import ch.elexis.core.model.BriefConstants;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IDocumentTemplate;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IDocumentStore;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.elexis.omnivore.model.TransientCategory;
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
		List<IDocument> existingLetters = letterDocumentStore.getDocuments(patient.getId(), null, null, null);
		Bundle results = client.search().forResource(DocumentReference.class)
				.where(DocumentReference.PATIENT.hasId(AllTests.getTestDatabaseInitializer().getPatient().getId()))
				.returnBundle(Bundle.class).execute();
		List<BundleEntryComponent> entries = results.getEntry();
		int existingEntriesSize = entries.size();
		assertEquals(existingDocuments.size() + existingLetters.size(), existingEntriesSize);

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
		attachment.setData("Test Text\n2te Zeile üöä!".getBytes());
		attachment.setCreation(Date.from(LocalDate.of(2000, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
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

		assertTrue(readAttachment.hasCreation());
		assertEquals(Date.from(LocalDate.of(2000, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()),
				readAttachment.getCreation());

		outcome = client.update().resource(readReference).execute();
		assertFalse(outcome.getCreated() == null ? false : outcome.getCreated());
	}

	@Test
	public void searchTemplateDocumentReference() throws IOException {
		DocumentReference reference = new DocumentReference();
		reference.setStatus(DocumentReferenceStatus.CURRENT);
		CodeableConcept storeIdConcept = reference.addCategory();
		storeIdConcept.addCoding(
				new Coding(CodingSystem.ELEXIS_DOCUMENT_STOREID.getSystem(), "ch.elexis.data.store.brief", null));
		CodeableConcept categoryConcept = new CodeableConcept(new Coding(
				CodingSystem.ELEXIS_DOCUMENT_CATEGORY.getSystem(), BriefConstants.TEMPLATE, BriefConstants.TEMPLATE));
		reference.addCategory(categoryConcept);

		DocumentReferenceContentComponent content = new DocumentReferenceContentComponent();
		Attachment attachment = new Attachment();
		attachment.setTitle("TestTemplatesSearch.docx");
		content.setAttachment(attachment);
		reference.addContent(content);

		MethodOutcome outcome = client.create().resource(reference).execute();
		assertNotNull(outcome);
		assertTrue(outcome.getCreated());
		assertNotNull(outcome.getId());
		reference = (DocumentReference) outcome.getResource();
		reference = uploadContent(reference,
				IOUtils.toByteArray(getClass().getResourceAsStream("/rsc/TestPlaceholders.docx")));

		Bundle results = client.search().forResource(DocumentReference.class)
				.where(DocumentReference.CATEGORY.exactly()
						.systemAndCode(CodingSystem.ELEXIS_DOCUMENT_CATEGORY.getSystem(), BriefConstants.TEMPLATE))
				.returnBundle(Bundle.class).execute();
		List<BundleEntryComponent> entries = results.getEntry();
		assertEquals(1, entries.size());

		IDocumentTemplate sysTemplate = CoreModelServiceHolder.get().create(IDocumentTemplate.class);
		sysTemplate.setCategory(new TransientCategory(BriefConstants.TEMPLATE));
		sysTemplate.setTemplateTyp(BriefConstants.SYS_TEMPLATE);
		sysTemplate.setTitle("OtherTestTemplatesSearch.docx");
		sysTemplate.setContent(getClass().getResourceAsStream("/rsc/TestPlaceholders.docx"));
		CoreModelServiceHolder.get().save(sysTemplate);

		results = client.search().forResource(DocumentReference.class)
				.where(DocumentReference.CATEGORY.exactly()
						.systemAndCode(CodingSystem.ELEXIS_DOCUMENT_CATEGORY.getSystem(), BriefConstants.TEMPLATE))
				.returnBundle(Bundle.class).execute();
		entries = results.getEntry();
		assertEquals(2, entries.size());

		Optional<DocumentReference> otherTemplate = entries.stream()
				.filter(e -> e.getResource() instanceof DocumentReference)
				.map(e -> (DocumentReference) e.getResource()).filter(d -> d.getContentFirstRep().getAttachment()
						.getTitle().equals("OtherTestTemplatesSearch.docx"))
				.findAny();
		assertTrue(otherTemplate.isPresent());
		assertTrue(CodeTypeUtil.isCodeInConceptList(otherTemplate.get().getCategory(),
				CodingSystem.ELEXIS_DOCUMENT_CATEGORY.getSystem(), BriefConstants.TEMPLATE));
		assertTrue(CodeTypeUtil.isCodeInConceptList(otherTemplate.get().getCategory(),
				CodingSystem.ELEXIS_DOCUMENT_TEMPLATE_TYP.getSystem(), BriefConstants.SYS_TEMPLATE));

		removeTemplates();
	}

	private void removeTemplates() {
		List<IDocumentTemplate> templates = letterDocumentStore.getDocumentTemplates(true);
		for (IDocumentTemplate iDocumentTemplate : templates) {
			letterDocumentStore.removeDocument(iDocumentTemplate);
		}
	}

	@Test
	public void createLetterDocumentReference() throws IOException {
		DocumentReference reference = new DocumentReference();
		reference.setStatus(DocumentReferenceStatus.CURRENT);
		reference.setSubject(
				(Reference) new Reference("Patient/" + AllTests.getTestDatabaseInitializer().getPatient().getId())
						.setId(AllTests.getTestDatabaseInitializer().getPatient().getId()));
		CodeableConcept storeIdConcept = reference.addCategory();
		storeIdConcept.addCoding(new Coding(CodingSystem.ELEXIS_DOCUMENT_STOREID.getSystem(),
				"ch.elexis.data.store.brief",
				"ch.elexis.data.store.brief"));
		CodeableConcept categoryConcept = new CodeableConcept(
				new Coding(CodingSystem.ELEXIS_DOCUMENT_CATEGORY.getSystem(), BriefConstants.UNKNOWN,
						BriefConstants.UNKNOWN));
		reference.addCategory(categoryConcept);
		
		DocumentReferenceContentComponent content = new DocumentReferenceContentComponent();
		Attachment attachment = new Attachment();
		attachment.setTitle("test attachment.txt");
		attachment.setData("Test Text\n2te Zeile üöä!".getBytes());
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
		
		Optional<String> storeid = FhirUtil.getCodeFromConceptList(CodingSystem.ELEXIS_DOCUMENT_STOREID.getSystem(),
				readReference.getCategory());
		assertTrue(storeid.isPresent());
		assertEquals("ch.elexis.data.store.brief", storeid.get());

		Optional<String> category = FhirUtil.getCodeFromConceptList(CodingSystem.ELEXIS_DOCUMENT_CATEGORY.getSystem(),
				readReference.getCategory());
		assertTrue(category.isPresent());
		assertEquals(BriefConstants.UNKNOWN, category.get());
	}

	@Test
	public void validateTemplate() throws ClientProtocolException, IOException, ElexisException {
		DocumentReference reference = new DocumentReference();
		reference.setStatus(DocumentReferenceStatus.CURRENT);
		CodeableConcept storeIdConcept = reference.addCategory();
		storeIdConcept.addCoding(
				new Coding(CodingSystem.ELEXIS_DOCUMENT_STOREID.getSystem(), "ch.elexis.data.store.brief", null));
		CodeableConcept categoryConcept = new CodeableConcept(new Coding(
				CodingSystem.ELEXIS_DOCUMENT_CATEGORY.getSystem(), BriefConstants.TEMPLATE, BriefConstants.TEMPLATE));
		reference.addCategory(categoryConcept);

		DocumentReferenceContentComponent content = new DocumentReferenceContentComponent();
		Attachment attachment = new Attachment();
		attachment.setTitle("TestPlaceholders.docx");
		content.setAttachment(attachment);
		reference.addContent(content);

		MethodOutcome outcome = client.create().resource(reference).execute();
		assertNotNull(outcome);
		assertTrue(outcome.getCreated());
		assertNotNull(outcome.getId());
		reference = (DocumentReference) outcome.getResource();
		reference = uploadContent(reference,
				IOUtils.toByteArray(getClass().getResourceAsStream("/rsc/TestPlaceholders.docx")));

		CodeableConcept context = new CodeableConcept();
		Parameters returnParameters = client.operation().onInstance(reference.getId()).named("$validatetemplate")
				.withParameters(new Parameters().addParameter("context", context)).execute();
		assertNotNull(returnParameters);
		assertTrue(returnParameters.getParameters("Patient") != null
				&& returnParameters.getParameters("Patient").size() == 1);

		context.addCoding(
				new Coding("Patient", "Patient/" + AllTests.getTestDatabaseInitializer().getPatient().getId(), null));
		returnParameters = client.operation().onInstance(reference.getId()).named("$validatetemplate")
				.withParameters(new Parameters().addParameter("context", context)).execute();
		assertNotNull(returnParameters);
		assertTrue(returnParameters.getParameters("Patient") == null
				|| returnParameters.getParameters("Patient").isEmpty());
	}

	@Test
	public void createFromTemplate() throws ClientProtocolException, IOException, ElexisException {
		DocumentReference reference = new DocumentReference();
		reference.setStatus(DocumentReferenceStatus.CURRENT);
		CodeableConcept storeIdConcept = reference.addCategory();
		storeIdConcept.addCoding(
				new Coding(CodingSystem.ELEXIS_DOCUMENT_STOREID.getSystem(), "ch.elexis.data.store.brief",
				null));
		CodeableConcept categoryConcept = new CodeableConcept(new Coding(
				CodingSystem.ELEXIS_DOCUMENT_CATEGORY.getSystem(), BriefConstants.TEMPLATE, BriefConstants.TEMPLATE));
		reference.addCategory(categoryConcept);

		DocumentReferenceContentComponent content = new DocumentReferenceContentComponent();
		Attachment attachment = new Attachment();
		attachment.setTitle("TestPlaceholders.docx");
		content.setAttachment(attachment);
		reference.addContent(content);

		MethodOutcome outcome = client.create().resource(reference).execute();
		assertNotNull(outcome);
		assertTrue(outcome.getCreated());
		assertNotNull(outcome.getId());
		reference = (DocumentReference) outcome.getResource();
		reference = uploadContent(reference,
				IOUtils.toByteArray(getClass().getResourceAsStream("/rsc/TestPlaceholders.docx")));
		
		CodeableConcept context = new CodeableConcept();
		context.addCoding(
				new Coding("Patient", "Patient/" + AllTests.getTestDatabaseInitializer().getPatient().getId(), null));
		context.addCoding(
				new Coding("Adressat", "Patient/" + AllTests.getTestDatabaseInitializer().getPatient().getId(), null));
		Parameters returnParameters = client.operation().onInstance(reference.getId()).named("$createdocument")
				.withParameters(new Parameters().addParameter("context", context)).execute();
		assertNotNull(returnParameters);
		assertTrue(returnParameters.getParameterFirstRep().getResource() instanceof DocumentReference);

		IDocumentTemplate sysTemplate = CoreModelServiceHolder.get().create(IDocumentTemplate.class);
		sysTemplate.setCategory(new TransientCategory(BriefConstants.TEMPLATE));
		sysTemplate.setTitle("OtherTestTemplatesSearch.docx");
		sysTemplate.setContent(getClass().getResourceAsStream("/rsc/TestPlaceholders.docx"));
		CoreModelServiceHolder.get().save(sysTemplate);

		Bundle results = client.search().forResource(DocumentReference.class)
				.where(DocumentReference.CATEGORY.exactly()
						.systemAndCode(CodingSystem.ELEXIS_DOCUMENT_CATEGORY.getSystem(), BriefConstants.TEMPLATE))
				.returnBundle(Bundle.class).execute();
		List<BundleEntryComponent> entries = results.getEntry();
		Optional<DocumentReference> otherTemplate = entries.stream()
				.filter(e -> e.getResource() instanceof DocumentReference).map(e -> (DocumentReference) e.getResource())
				.filter(d -> d.getContentFirstRep().getAttachment().getTitle().equals("OtherTestTemplatesSearch.docx"))
				.findAny();
		assertTrue(otherTemplate.isPresent());

		returnParameters = client.operation().onInstance(otherTemplate.get().getId()).named("$createdocument")
				.withParameters(new Parameters().addParameter("context", context)).execute();
		assertNotNull(returnParameters);
		assertTrue(returnParameters.getParameterFirstRep().getResource() instanceof DocumentReference);

		// test delete created document
		results = client.search().forResource(DocumentReference.class)
				.where(DocumentReference.PATIENT.hasId(AllTests.getTestDatabaseInitializer().getPatient().getId()))
				.returnBundle(Bundle.class).execute();
		entries = results.getEntry();
		List<DocumentReference> found = entries.stream().filter(e -> e.getResource() instanceof DocumentReference)
				.map(e -> (DocumentReference) e.getResource())
				.filter(d -> d.getContentFirstRep().getAttachment().getTitle().equals("OtherTestTemplatesSearch.docx"))
				.collect(Collectors.toList());
		assertFalse(found.isEmpty());
		for (DocumentReference documentReference : found) {
			client.delete().resource(documentReference).execute();
		}
		results = client.search().forResource(DocumentReference.class)
				.where(DocumentReference.PATIENT.hasId(AllTests.getTestDatabaseInitializer().getPatient().getId()))
				.returnBundle(Bundle.class).execute();
		entries = results.getEntry();
		found = entries.stream().filter(e -> e.getResource() instanceof DocumentReference)
				.map(e -> (DocumentReference) e.getResource())
				.filter(d -> d.getContentFirstRep().getAttachment().getTitle().equals("OtherTestTemplatesSearch.docx"))
				.collect(Collectors.toList());
		assertTrue(found.isEmpty());

		removeTemplates();
	}

	private DocumentReference uploadContent(DocumentReference documentReference, byte[] content)
			throws ClientProtocolException, IOException {
		// upload content
		CloseableHttpClient ourHttpClient = HttpClients.createDefault();
		HttpPost post = new HttpPost(
				client.getServerBase() + "/" + documentReference.getId() + "/$binary-access-write");
		post.setEntity(new ByteArrayEntity(content, ContentType.APPLICATION_OCTET_STREAM));
		try (CloseableHttpResponse resp = ourHttpClient.execute(post)) {
			assertEquals(200, resp.getStatusLine().getStatusCode());

			String response = IOUtils.toString(resp.getEntity().getContent(), Constants.CHARSET_UTF8);
			return client.getFhirContext().newJsonParser().parseResource(DocumentReference.class, response);
		}
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
