package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.codesystems.ConditionCategory;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import ch.elexis.core.fhir.FhirConstants;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.test.initializer.TestDatabaseInitializer;
import info.elexis.server.fhir.rest.core.test.AllTests;
import info.elexis.server.fhir.rest.core.test.FhirUtil;

public class ConditionTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		AllTests.getTestDatabaseInitializer().initializeBehandlung();
		AllTests.getTestDatabaseInitializer().initializeAUF();

		client = FhirUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);

	}

	@Test
	public void getCondition() {
		Patient readPatient = client.read().resource(Patient.class)
				.withId(AllTests.getTestDatabaseInitializer().getPatient().getId()).execute();

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

		// search by patient and category
		results = client.search().forResource(Condition.class).where(Condition.SUBJECT.hasId(readPatient.getId()))
				.and(Condition.CATEGORY.exactly().code("problem-list-item")).returnBundle(Bundle.class).execute();
		assertNotNull(results);
		entries = results.getEntry();
		assertFalse(entries.isEmpty());

		results = client.search().forResource(Condition.class).where(Condition.SUBJECT.hasId(readPatient.getId()))
				.and(Condition.CATEGORY.exactly().code("abc")).returnBundle(Bundle.class).execute();
		assertNotNull(results);
		entries = results.getEntry();
		assertTrue(entries.isEmpty());

	}

	@Test
	public void createCondition() {
		Condition condition = new Condition();

		Narrative narrative = new Narrative();
		String divEncodedText = "Test\nText".replaceAll("(\r\n|\r|\n)", "<br />");
		narrative.setDivAsString(divEncodedText);
		condition.setText(narrative);
		condition.setSubject(new Reference("Patient/" + AllTests.getTestDatabaseInitializer().getPatient().getId()));
		condition.addCategory(new CodeableConcept().addCoding(new Coding(ConditionCategory.PROBLEMLISTITEM.getSystem(),
				ConditionCategory.PROBLEMLISTITEM.toCode(), ConditionCategory.PROBLEMLISTITEM.getDisplay())));

		MethodOutcome outcome = client.create().resource(condition).execute();
		assertNotNull(outcome);
		assertTrue(outcome.getCreated());
		assertNotNull(outcome.getId());

		Condition readCondition = client.read().resource(Condition.class).withId(outcome.getId()).execute();
		assertNotNull(readCondition);
		assertEquals(outcome.getId().getIdPart(), readCondition.getIdElement().getIdPart());
		assertEquals(condition.getCategory().get(0).getCoding().get(0).getCode(),
				readCondition.getCategory().get(0).getCoding().get(0).getCode());
	}

	/**
	 * Test diagnose property set by
	 * {@link TestDatabaseInitializer#initializePatient()}.
	 */
	@Test
	public void getConditionProperties() {
		Patient readPatient = client.read().resource(Patient.class)
				.withId(AllTests.getTestDatabaseInitializer().getPatient().getId()).execute();

		// search by patient and category
		Bundle results = client.search().forResource(Condition.class)
				.where(Condition.SUBJECT.hasId(readPatient.getId()))
				.and(Condition.CATEGORY.exactly().code("problem-list-item")).returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		Condition condition = (Condition) entries.get(0).getResource();
		assertNotNull(condition);

		assertEquals("Patient/" + AllTests.getTestDatabaseInitializer().getPatient().getId(),
				condition.getSubject().getReference());
		assertNotNull(condition.getCategory());
		assertNotNull(condition.getCategory().get(0).getCoding());
		assertFalse(condition.getCategory().get(0).getCoding().isEmpty());
		assertEquals("problem-list-item", condition.getCategory().get(0).getCoding().get(0).getCode());
	}

	@Test
	public void findAUFCondition() {
		Patient readPatient = client.read().resource(Patient.class)
				.withId(AllTests.getTestDatabaseInitializer().getPatient().getId()).execute();

		ICriterion<TokenClientParam> aufCondition = Condition.CODE.exactly().systemAndCode(FhirConstants.DE_EAU_SYSTEM,
				FhirConstants.DE_EAU_SYSTEM_CODE);
		Bundle results = client.search().forResource(Condition.class)
				.where(Condition.SUBJECT.hasId(readPatient.getId())).where(aufCondition).returnBundle(Bundle.class)
				.execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		Condition condition = (Condition) entries.get(0).getResource();
		assertNotNull(condition);
		assertEquals("Patient/" + AllTests.getTestDatabaseInitializer().getPatient().getId(),
				condition.getSubject().getReference());

		List<Coding> coding = condition.getCode().getCoding();
		assertEquals(2, coding.size());

		Coding codingFirstRep = condition.getStageFirstRep().getType().getCodingFirstRep();
		assertEquals(CodingSystem.ELEXIS_AUF_DEGREE.getSystem(), codingFirstRep.getSystem());
		assertEquals("75", codingFirstRep.getCode());

		assertEquals("note", condition.getNote().get(0).getText());

		assertEquals(LocalDate.now().getDayOfMonth(), condition.getOnsetDateTimeType().getDay().intValue());
	}

	@Test
	public void loadAUFCondition() {
		Condition sickCertificate = client.read().resource(Condition.class)
				.withId(AllTests.getTestDatabaseInitializer().getAUFs().getId()).execute();
		assertEquals("Patient/" + AllTests.getTestDatabaseInitializer().getPatient().getId(),
				sickCertificate.getSubject().getReference());
	}

	@Test
	@Ignore(value = "unfinished")
	public void createAUFCondition() {
		Condition condition = new Condition();
		condition.getCode().getCoding().add(new Coding(FhirConstants.DE_EAU_SYSTEM, FhirConstants.DE_EAU_SYSTEM_CODE,
				FhirConstants.DE_EAU_SYSTEM_CODE));
		condition.getCode().getCoding()
				.add(new Coding(CodingSystem.ELEXIS_AUF_REASON.getSystem(), "Schwangerschaft", "Schawngerschaft"));
		condition.setSubject(new Reference("Patient/" + AllTests.getTestDatabaseInitializer().getPatient().getId()));
		condition.setOnset(new DateTimeType(new Date()));

	}
}
