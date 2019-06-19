package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Coverage;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.exceptions.FHIRException;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.hapi.fhir.FhirUtil;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.test.initializer.TestDatabaseInitializer;
import info.elexis.server.fhir.rest.core.test.AllTests;

public class CoverageTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		AllTests.getTestDatabaseInitializer().initializeFall();

		List<ICoverage> faelle = AllTests.getTestDatabaseInitializer().getPatient().getCoverages();
		assertFalse(faelle.isEmpty());

		client = FhirUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
	}

	@Test
	public void getCoverage() {
		Patient readPatient = client.read().resource(Patient.class).withId(AllTests.getTestDatabaseInitializer().getPatient().getId())
				.execute();
		// search by BENEFICIARY
		Bundle results = client.search().forResource(Coverage.class)
				.where(Coverage.BENEFICIARY.hasId(readPatient.getId()))
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
	}

	@Test
	public void createCoverage() {
		Coverage coverage = new Coverage();
		// minimal coverage information
		coverage.setBeneficiary(new Reference(new IdDt("Patient", AllTests.getTestDatabaseInitializer().getPatient().getId())));
		coverage.setType(
				new CodeableConcept().addCoding(new Coding(CodingSystem.ELEXIS_COVERAGE_TYPE.getSystem(), "KVG", "")));
		
		MethodOutcome outcome = client.create().resource(coverage).execute();
		assertNotNull(outcome);
		assertTrue(outcome.getCreated());
		assertNotNull(outcome.getId());

		Coverage readCoverage = client.read().resource(Coverage.class).withId(outcome.getId()).execute();
		assertNotNull(readCoverage);
		assertEquals(outcome.getId().getIdPart(), readCoverage.getIdElement().getIdPart());
		assertNotNull(readCoverage.getPeriod().getStart());
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

		Reference beneficiary = readCoverage.getBeneficiary();
		assertNotNull(beneficiary);
		assertEquals("Patient/" + AllTests.getTestDatabaseInitializer().getPatient().getId(),
				beneficiary.getReference());
		List<Reference> payors = readCoverage.getPayor();
		assertNotNull(payors);
		assertFalse(payors.isEmpty());
		assertEquals("Organization/" + TestDatabaseInitializer.getOrganization().getId(),
				payors.get(0).getReference());
		Period period = readCoverage.getPeriod();
		assertNotNull(period);
		assertEquals(LocalDate.of(2016, Month.SEPTEMBER, 1),
				AllTests.getLocalDateTime(period.getStart()).toLocalDate());
		assertTrue(period.getEnd() == null);
		assertEquals("1234-5678", readCoverage.getDependent());

		Narrative narrative = readCoverage.getText();
		assertNotNull(narrative);
		String text = narrative.getDivAsString();
		assertNotNull(text);
		assertTrue(text.contains("Test Fall(01.09.2016-offen)"));
	}
}
