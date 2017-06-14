package es.fhir.rest.core.resources;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Claim;
import org.hl7.fhir.dstu3.model.Claim.ItemComponent;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Coverage;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.hl7.fhir.dstu3.model.StringType;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.util.ModelUtil;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;
import info.elexis.server.core.connector.elexis.services.VerrechnetService;

public class ClaimTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		TestDatabaseInitializer initializer = new TestDatabaseInitializer();
		initializer.initializeTarmedTables();
		initializer.initializeBehandlung();

		client = ModelUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
	}

	@Test
	public void createClaim() {
		// claim needs a coverage, a diagnose, a practitioner, an encounter and
		// items
		Coverage coverage = new Coverage();
		// minimal coverage information
		coverage.setBeneficiary(new Reference(new IdDt("Patient", TestDatabaseInitializer.getPatient().getId())));
		coverage.setType(new CodeableConcept()
				.addCoding(new Coding(CodingSystem.ELEXIS_COVERAGE_TYPE.getSystem(), "KVG", "Test")));
		MethodOutcome coverageOutcome = client.create().resource(coverage).execute();
		assertTrue(coverageOutcome.getCreated());

		Bundle results = client.search().forResource(Encounter.class).where(Encounter.IDENTIFIER.exactly()
				.systemAndIdentifier("www.elexis.info/consultationid", TestDatabaseInitializer.getBehandlung().getId()))
				.returnBundle(Bundle.class).execute();
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		Encounter encounter = (Encounter) entries.get(0).getResource();
		
		Claim claim = new Claim();
		claim.addInsurance().setCoverage(new Reference("Coverage/" + coverageOutcome.getId()));
		claim.setProvider(new Reference("Practitioner/" + TestDatabaseInitializer.getMandant().getId()));
		claim.addDiagnosis().setDiagnosis(new CodeableConcept()
				.addCoding(new Coding(CodingSystem.ELEXIS_DIAGNOSE_TESSINERCODE.getSystem(), "A1", "")));
		claim.addInformation().setValue(new StringType("Encounter/" + encounter.getId()));

		ItemComponent item = claim.addItem();
		item.setQuantity((SimpleQuantity) new SimpleQuantity().setValue(1));
		item.setService(new CodeableConcept().addCoding(new Coding("www.elexis.info/billing/tarmed", "00.0010", "")));

		List<Verrechnet> before = VerrechnetService.findAll(false);
		MethodOutcome outcome = client.create().resource(claim).execute();
		assertNotNull(outcome);
		assertTrue(outcome.getCreated());
		List<Verrechnet> after = VerrechnetService.findAll(false);
		assertTrue(after.size() - before.size() == 1);

		// "00.0010" can only be billed once ...
		boolean failed = false;
		try {
			outcome = client.create().resource(claim).execute();
		} catch (InternalErrorException e) {
			failed = true;
		}
		assertTrue(failed);
	}
}
