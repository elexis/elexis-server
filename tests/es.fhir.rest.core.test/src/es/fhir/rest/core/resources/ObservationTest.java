package es.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Observation;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.IGenericClient;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.util.ModelUtil;
import es.fhir.rest.core.test.AllTests;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;

public class ObservationTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() {
		TestDatabaseInitializer initializer = new TestDatabaseInitializer();
		initializer.initializeLabResult();

		client = ModelUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
	}

	@Test
	public void getObservation() {
		Observation readObservation = client.read().resource(Observation.class)
				.withId(TestDatabaseInitializer.getLabResult().getId())
				.execute();
		assertNotNull(readObservation);

		// search by patient and category
		Bundle results = client.search().forResource(Observation.class)
				.where(Observation.SUBJECT.hasId(TestDatabaseInitializer.getPatient().getId()))
				.and(Condition.CATEGORY.exactly().code("laboratory"))
				.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		Observation observation = (Observation) entries.get(0).getResource();
		assertEquals(observation.getIdElement().getIdPart(), TestDatabaseInitializer.getLabResult().getId());

		results = client.search().forResource(Observation.class)
				.where(Observation.SUBJECT.hasId(TestDatabaseInitializer.getPatient().getId()))
				.and(Condition.CATEGORY.exactly().code("abc"))
				.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		entries = results.getEntry();
		assertTrue(entries.isEmpty());

		// search with date parameter
		results = client.search().forResource(Observation.class)
				.where(Observation.SUBJECT.hasId(TestDatabaseInitializer.getPatient().getId()))
				.and(Observation.DATE.exactly()
						.day(AllTests.getDate(LocalDateTime.of(2016, Month.DECEMBER, 14, 17, 44, 25))))
				.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		entries = results.getEntry();
		assertFalse(entries.isEmpty());

		results = client.search().forResource(Observation.class)
				.where(Observation.SUBJECT.hasId(TestDatabaseInitializer.getPatient().getId()))
				.and(Observation.DATE.exactly()
						.day(AllTests.getDate(LocalDateTime.of(2016, Month.DECEMBER, 1, 0, 0, 0))))
				.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		entries = results.getEntry();
		assertTrue(entries.isEmpty());

		// search with date parameter and code
		results = client.search().forResource(Observation.class)
				.where(Observation.SUBJECT.hasId(TestDatabaseInitializer.getPatient().getId()))
				.and(Observation.CODE.exactly()
						.systemAndCode(CodingSystem.ELEXIS_LOCAL_LABORATORY_VITOLABKEY.getSystem(), "2"))
				.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		entries = results.getEntry();
		assertFalse(entries.isEmpty());
	}

	@Test
	public void createObservation() {

	}

	/**
	 * Test all properties set by
	 * {@link TestDatabaseInitializer#initializeBehandlung()}.
	 */
	@Test
	public void getObservationProperties() {
		Observation readObservation = client.read().resource(Observation.class)
				.withId(TestDatabaseInitializer.getLabResult().getId()).execute();
		assertNotNull(readObservation);
	}
}
