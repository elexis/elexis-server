package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.ch.BillingLaw;
import ch.elexis.core.test.initializer.TestDatabaseInitializer;
import ch.elexis.core.time.TimeUtil;
import info.elexis.server.fhir.rest.core.test.AllTests;
import info.elexis.server.fhir.rest.core.test.FhirUtil;

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
		Patient readPatient = client.read().resource(Patient.class)
				.withId(AllTests.getTestDatabaseInitializer().getPatient().getId()).execute();
		// search by BENEFICIARY
		Bundle results = client.search().forResource(Coverage.class)
				.where(Coverage.BENEFICIARY.hasId(readPatient.getId())).returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		Coverage coverage = (Coverage) entries.get(0).getResource();
		// read with by id
		Coverage readCoverage = client.read().resource(Coverage.class).withId(coverage.getId()).execute();
		assertNotNull(readCoverage);
		assertEquals(coverage.getId(), readCoverage.getId());
	}

	@Test
	public void createUpdateDeleteCoverage() {

		String testPatientId = AllTests.getTestDatabaseInitializer().getPatient().getId();

		Coverage coverage = new Coverage();
		// minimal coverage information
		coverage.setBeneficiary(new Reference(new IdDt("Patient", testPatientId)));
		coverage.setType(
				new CodeableConcept().addCoding(new Coding(CodingSystem.ELEXIS_COVERAGE_TYPE.getSystem(), "KVG", "")));
		LocalDateTime startDate = LocalDate.of(2024, 8, 5).atStartOfDay();
		coverage.setPeriod(new Period().setStart(TimeUtil.toDate(startDate)));

		MethodOutcome createOutcome = client.create().resource(coverage).execute();
		assertNotNull(createOutcome);
		assertTrue(createOutcome.getCreated());
		assertNotNull(createOutcome.getId());

		Coverage readCoverage = client.read().resource(Coverage.class).withId(createOutcome.getId()).execute();
		assertNotNull(readCoverage);

		// test initial attributes
		assertEquals("Patient/" + testPatientId, readCoverage.getBeneficiary().getReference());
		assertEquals(createOutcome.getId().getIdPart(), readCoverage.getIdElement().getIdPart());
		assertEquals("KVG", FhirUtil.getCodeFromCodingList(CodingSystem.ELEXIS_COVERAGE_TYPE.getSystem(),
				readCoverage.getType().getCoding()).orElse(null));
		assertEquals("Krankheit", FhirUtil.getCodeFromCodingList(CodingSystem.ELEXIS_COVERAGE_REASON.getSystem(),
				readCoverage.getType().getCoding()).orElse(null));
		assertEquals("Patient/" + testPatientId, readCoverage.getPolicyHolder().getReference());
		assertNull(readCoverage.getPayorFirstRep().getReference());
		assertEquals(TimeUtil.toDate(startDate).getTime(), readCoverage.getPeriod().getStart().getTime());
		assertNull(readCoverage.getPeriod().getEnd());
		assertEquals(
				"<div xmlns=\"http://www.w3.org/1999/xhtml\">KVG: Krankheit - online created(05.08.2024-offen)</div>",
				readCoverage.getText().getDivAsString());

		// update
		readCoverage.setType(
				new CodeableConcept().addCoding(new Coding(CodingSystem.ELEXIS_COVERAGE_TYPE.getSystem(), "UVG", ""))
						.addCoding(new Coding(CodingSystem.ELEXIS_COVERAGE_REASON.getSystem(), "KingOfMyCastle", "")));
		LocalDate newStartDate = LocalDate.of(2024, 6, 14);
		LocalDate newEndDate = LocalDate.of(2024, 6, 20);
		readCoverage
				.setPeriod(new Period().setStart(TimeUtil.toDate(newStartDate)).setEnd(TimeUtil.toDate(newEndDate)));
		readCoverage.setPayor(null); // #TODO Kostenträger
		// TODO Versicherungsnummer
		// TODO Unfallnummer
		// TODO Unfalldatum
//		Narrative narrative = new Narrative();
//		narrative.setDivAsString("Thats the new narrative");
//		readCoverage.setText(narrative);
		MethodOutcome updateOutcome = client.update().resource(readCoverage).execute();
		assertNotNull(updateOutcome);

		readCoverage = client.read().resource(Coverage.class).withId(createOutcome.getId()).execute();

		assertEquals("UVG", FhirUtil.getCodeFromCodingList(CodingSystem.ELEXIS_COVERAGE_TYPE.getSystem(),
				readCoverage.getType().getCoding()).orElse(null));
		assertEquals("KingOfMyCastle", FhirUtil.getCodeFromCodingList(CodingSystem.ELEXIS_COVERAGE_REASON.getSystem(),
				readCoverage.getType().getCoding()).orElse(null));
//		assertEquals(
//				"<div xmlns=\"http://www.w3.org/1999/xhtml\">UVG: KingOfMyCastle - Thats the new narrative(05.08.2024-offen)</div>",
//				readCoverage.getText().getDivAsString());
		assertEquals(TimeUtil.toDate(newStartDate.atStartOfDay()).getTime(),
				readCoverage.getPeriod().getStart().getTime());
		assertEquals(TimeUtil.toDate(newEndDate.atStartOfDay()).getTime(), readCoverage.getPeriod().getEnd().getTime());

		MethodOutcome deleteOutcome = client.delete().resourceById(readCoverage.getIdElement()).execute();
		assertNotNull(deleteOutcome);
	}

	/**
	 * Test all properties set by {@link TestDatabaseInitializer#initializeFall()}.
	 * 
	 * @throws FHIRException
	 */
	@Test
	public void getOrganizationProperties() throws FHIRException {
		Coverage readCoverage = client.read().resource(Coverage.class).withId(TestDatabaseInitializer.getFall().getId())
				.execute();
		assertNotNull(readCoverage);

		Reference beneficiary = readCoverage.getBeneficiary();
		assertNotNull(beneficiary);
		assertEquals("Patient/" + AllTests.getTestDatabaseInitializer().getPatient().getId(),
				beneficiary.getReference());
		List<Reference> payors = readCoverage.getPayor();
		assertNotNull(payors);
		assertFalse(payors.isEmpty());
		assertEquals("Organization/" + TestDatabaseInitializer.getOrganization().getId(), payors.get(0).getReference());
		Period period = readCoverage.getPeriod();
		assertNotNull(period);
		assertEquals(LocalDate.of(2016, Month.SEPTEMBER, 1),
				AllTests.getLocalDateTime(period.getStart()).toLocalDate());
		assertTrue(period.getEnd() == null);
		if (TestDatabaseInitializer.getFall().getBillingSystem().getLaw() == BillingLaw.KVG) {
			Optional<Identifier> found = readCoverage.getIdentifier().stream()
					.filter(i -> StringUtils.isNotBlank(i.getValue()) && "1234-5678".equals(i.getValue())).findFirst();
			assertTrue(found.isPresent());
		} else if (TestDatabaseInitializer.getFall().getBillingSystem().getLaw() == BillingLaw.UVG
				|| TestDatabaseInitializer.getFall().getBillingSystem().getLaw() == BillingLaw.IV) {
			assertEquals("1234-5678", readCoverage.getDependent());
		}

		Narrative narrative = readCoverage.getText();
		assertNotNull(narrative);
		String text = narrative.getDivAsString();
		assertNotNull(text);
		assertTrue(text.contains("Test Fall(01.09.2016-offen)"));
	}
}
