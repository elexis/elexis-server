package es.fhir.rest.core.resources;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.MedicationOrder;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.IGenericClient;
import es.fhir.rest.core.test.FhirClient;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;

public class MedicationOrderTest {

	private static IGenericClient client;

	private static Patient patient;

	@BeforeClass
	public static void setupClass() {
		TestDatabaseInitializer initializer = new TestDatabaseInitializer();
		initializer.initializePrescription();

		client = FhirClient.getTestClient();
		assertNotNull(client);
		Bundle results = client.search().forResource(Patient.class).where(Patient.NAME.matches().value("Test"))
				.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		patient = (Patient) entries.get(0).getResource();
	}

	@Test
	public void getMedicationOrder() {
		Bundle results = client.search().forResource(MedicationOrder.class)
				.where(MedicationOrder.PATIENT.hasId(patient.getIdElement().getIdPart())).returnBundle(Bundle.class)
				.execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
	}
}
