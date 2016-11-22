package es.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import ch.elexis.core.findings.util.ModelUtil;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;

public class ProcedureRequestTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() {
		TestDatabaseInitializer initializer = new TestDatabaseInitializer();
		initializer.initializeBehandlung();

		client = ModelUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
	}

	@Test
	public void createProcedureRequest() {
		Bundle results = client.search().forResource(Encounter.class).where(Encounter.IDENTIFIER.exactly()
				.systemAndIdentifier("www.elexis.info/consultationid", TestDatabaseInitializer.getBehandlung().getId()))
				.returnBundle(Bundle.class).execute();
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		Encounter encounter = (Encounter) entries.get(0).getResource();
		
		ProcedureRequest procedureRequest = new ProcedureRequest();

		Narrative narrative = new Narrative();
		String divEncodedText = "Test\nText".replaceAll("(\r\n|\r|\n)", "<br />");
		narrative.setDivAsString(divEncodedText);
		procedureRequest.setText(narrative);
		procedureRequest.setSubject(new Reference("Patient/" + TestDatabaseInitializer.getPatient().getId()));
		procedureRequest.setEncounter(new Reference("Encounter/" + encounter.getId()));

		MethodOutcome outcome = client.create().resource(procedureRequest).execute();
		assertNotNull(outcome);
		assertTrue(outcome.getCreated());
		assertNotNull(outcome.getId());

		ProcedureRequest readProcedureRequest = client.read().resource(ProcedureRequest.class).withId(outcome.getId())
				.execute();
		assertNotNull(readProcedureRequest);
		assertEquals(outcome.getId().getIdPart(), readProcedureRequest.getIdElement().getIdPart());
		assertEquals(procedureRequest.getSubject().getReferenceElement().getIdPart(),
				readProcedureRequest.getSubject().getReferenceElement().getIdPart());
		assertEquals(procedureRequest.getEncounter().getReferenceElement().getIdPart(),
				readProcedureRequest.getEncounter().getReferenceElement().getIdPart());
		assertTrue(readProcedureRequest.getText().getDivAsString().contains("Test"));
	}
}
