package es.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.IGenericClient;
import es.fhir.rest.core.test.FhirClient;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;

public class EncounterTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() {
		TestDatabaseInitializer initializer = new TestDatabaseInitializer();
		initializer.initializeBehandlung();

		client = FhirClient.getTestClient();
		assertNotNull(client);

	}

	@Test
	public void getEncounter() {
		Patient readPatient = client.read().resource(Patient.class).withId(TestDatabaseInitializer.getPatient().getId())
				.execute();
		// search by patient
		Bundle results = client.search().forResource(Encounter.class)
				.where(Encounter.PATIENT.hasId(readPatient.getId())).returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		Encounter encounter = (Encounter) entries.get(0).getResource();
		// read with by id
		Encounter readEncounter = client.read().resource(Encounter.class).withId(encounter.getId()).execute();
		assertNotNull(readEncounter);
		assertEquals(encounter.getId(), readEncounter.getId());
	}
}
