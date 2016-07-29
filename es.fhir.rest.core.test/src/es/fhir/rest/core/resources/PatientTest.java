package es.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.IGenericClient;
import es.fhir.rest.core.test.FhirClient;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;

public class PatientTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() {
		TestDatabaseInitializer initializer = new TestDatabaseInitializer();
		initializer.initializePatient();

		client = FhirClient.getTestClient();
		assertNotNull(client);

	}

	@Test
	public void getPatient() {
		// search by name
		Bundle results = client.search().forResource(Patient.class).where(Patient.NAME.matches().value("Test"))
				.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		Patient patient = (Patient) entries.get(0).getResource();
		// read with by id
		Patient readPatient = client.read().resource(Patient.class).withId(patient.getId()).execute();
		assertNotNull(readPatient);
		assertEquals(patient.getId(), readPatient.getId());
		// search by elexis patient number
		results = client.search().byUrl("Patient?patientNumber=" + getPatientNumber(patient)).returnBundle(Bundle.class)
				.execute();
		assertNotNull(results);
		entries = results.getEntry();
		assertFalse(entries.isEmpty());
	}

	public int getPatientNumber(Patient patient) {
		List<Identifier> identifiers = patient.getIdentifier();
		for (Identifier identifier : identifiers) {
			if ("www.elexis.info/patnr".equals(identifier.getSystem())) {
				return Integer.parseInt(identifier.getValue());
			}
		}
		return -1;
	}
}
