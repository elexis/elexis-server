package es.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.IGenericClient;
import es.fhir.rest.core.test.FhirClient;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;

public class ConditionTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() {
		TestDatabaseInitializer initializer = new TestDatabaseInitializer();
		initializer.initializeBehandlung();

		client = FhirClient.getTestClient();
		assertNotNull(client);

	}

	@Test
	public void getCondition() {
		Patient readPatient = client.read().resource(Patient.class).withId(TestDatabaseInitializer.getPatient().getId())
				.execute();
				
		// search by patient
		Bundle results = client.search().forResource(Condition.class)
				.where(Condition.SUBJECT.hasId(readPatient.getId())).returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		Condition condition = (Condition) entries.get(0).getResource();

		// read with by id
		Condition readCondition = client.read().resource(Condition.class).withId(condition.getId()).execute();
		assertNotNull(readCondition);
		assertEquals(condition.getId(), readCondition.getId());
	}

	/**
	 * Test diagnose property set by
	 * {@link TestDatabaseInitializer#initializePatient()}.
	 */
	@Test
	public void getConditionProperties() {
		Patient readPatient = client.read().resource(Patient.class).withId(TestDatabaseInitializer.getPatient().getId())
				.execute();

		// search by patient
		Bundle results = client.search().forResource(Condition.class)
				.where(Condition.SUBJECT.hasId(readPatient.getId())).returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		Condition condition = (Condition) entries.get(0).getResource();

		assertEquals("Patient/" + TestDatabaseInitializer.getPatient().getId(), condition.getSubject().getReference());
		assertEquals("active", condition.getClinicalStatus().toCode());
		assertNotNull(condition.getCategory());
		assertNotNull(condition.getCategory().getCoding());
		assertFalse(condition.getCategory().getCoding().isEmpty());
		assertEquals("diagnosis", condition.getCategory().getCoding().get(0).getCode());
		assertNotNull(condition.getCode());
		assertNotNull(condition.getCode().getCoding());
		assertFalse(condition.getCode().getCoding().isEmpty());
		assertEquals("www.elexis.info/diagnosis/codes/praxis", condition.getCode().getCoding().get(0).getSystem());
		assertEquals("freetext", condition.getCode().getCoding().get(0).getCode());
		assertTrue(condition.getCode().getCoding().get(0).getDisplay().contains("Diagnose 1"));
	}
}
