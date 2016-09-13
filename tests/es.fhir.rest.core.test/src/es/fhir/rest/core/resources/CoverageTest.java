package es.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.hl7.fhir.dstu3.exceptions.FHIRException;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Coverage;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.IGenericClient;
import es.fhir.rest.core.test.AllTests;
import es.fhir.rest.core.test.FhirClient;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;

public class CoverageTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() {
		TestDatabaseInitializer initializer = new TestDatabaseInitializer();
		initializer.initializeFall();

		List<Fall> faelle = TestDatabaseInitializer.getPatient().getFaelle();
		assertFalse(faelle.isEmpty());

		client = FhirClient.getTestClient();
		assertNotNull(client);
	}

	@Test
	public void getCoverage() {
		// search by name
		Bundle results = client.search().forResource(Coverage.class)
				.where(Coverage.BENEFICIARYIDENTIFIER.exactly().code(TestDatabaseInitializer.getPatient().getId()))
				.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		Coverage coverage = (Coverage) entries.get(0).getResource();
		// read with by id
		Coverage readCoverage = client.read().resource(Coverage.class).withId(coverage.getId())
				.execute();
		assertNotNull(readCoverage);
		assertEquals(coverage.getId(), readCoverage.getId());
		// search by patientId
		results = client.search().byUrl("Coverage?patientId=" + TestDatabaseInitializer.getPatient().getId())
				.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		entries = results.getEntry();
		assertFalse(entries.isEmpty());
	}

	/**
	 * Test all properties set by
	 * {@link TestDatabaseInitializer#initializeFall()}.
	 * 
	 * @throws FHIRException
	 */
	@Test
	public void getOrganizationProperties() throws FHIRException {
		Coverage readCoverage = client.read().resource(Coverage.class)
				.withId(TestDatabaseInitializer.getFall().getId()).execute();
		assertNotNull(readCoverage);

		Reference beneficiary = readCoverage.getBeneficiaryReference();
		assertNotNull(beneficiary);
		assertEquals("Patient/" + TestDatabaseInitializer.getPatient().getId(),
				beneficiary.getReference());
		Reference issuer = readCoverage.getIssuerReference();
		assertNotNull(issuer);
		assertEquals("Organization/" + TestDatabaseInitializer.getOrganization().getId(),
				issuer.getReference());
		Period period = readCoverage.getPeriod();
		assertNotNull(period);
		assertEquals(LocalDate.of(2016, Month.SEPTEMBER, 1),
				AllTests.getLocalDateTime(period.getStart()).toLocalDate());
		assertTrue(period.getEnd() == null);
		assertEquals("1234-5678", readCoverage.getBin());
	}
}
