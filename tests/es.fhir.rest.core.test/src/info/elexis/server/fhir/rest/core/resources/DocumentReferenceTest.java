package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContentComponent;
import org.hl7.fhir.r4.model.Enumerations.DocumentReferenceStatus;
import org.hl7.fhir.r4.model.Reference;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import info.elexis.server.fhir.rest.core.test.AllTests;
import info.elexis.server.fhir.rest.core.test.FhirUtil;

public class DocumentReferenceTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		AllTests.getTestDatabaseInitializer().initializeBehandlung();

		client = FhirUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);

	}

	@Test
	public void getDocumentReference() {
		DocumentReference reference = new DocumentReference();
		reference.setStatus(DocumentReferenceStatus.CURRENT);
		reference.setSubject(
				(Reference) new Reference("Patient/" + AllTests.getTestDatabaseInitializer().getPatient().getId())
						.setId(AllTests.getTestDatabaseInitializer().getPatient().getId()));

		DocumentReferenceContentComponent content = new DocumentReferenceContentComponent();
		Attachment attachment = new Attachment();
		attachment.setTitle("test attachment.txt");
		attachment.setData(Base64.getEncoder().encode("Test get Text\n2te Zeile üöä!".getBytes()));
		content.setAttachment(attachment);
		reference.addContent(content);

		MethodOutcome outcome = client.create().resource(reference).execute();
		assertNotNull(outcome);
		assertTrue(outcome.getCreated());

		// search by patient
		Bundle results = client.search().forResource(DocumentReference.class)
				.where(DocumentReference.PATIENT.hasId(AllTests.getTestDatabaseInitializer().getPatient().getId()))
				.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
	}

	@Test
	public void createDocumentReference() {
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
		assertEquals(new String(reference.getContent().get(0).getAttachment().getData()),
				new String(readReference.getContent().get(0).getAttachment().getData()));


	}
}
