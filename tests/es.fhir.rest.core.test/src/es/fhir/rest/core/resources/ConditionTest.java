package es.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.instance.model.valuesets.ConditionCategory;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import ch.elexis.core.findings.util.ModelUtil;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;

public class ConditionTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		TestDatabaseInitializer initializer = new TestDatabaseInitializer();
		initializer.initializeBehandlung();

		client = ModelUtil.getGenericClient("http://localhost:8380/fhir");
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
		condition.setSubject(new Reference("Patient/" + TestDatabaseInitializer.getPatient().getId()));
		condition.addCategory(new CodeableConcept().addCoding(new Coding(ConditionCategory.COMPLAINT.getSystem(),
				ConditionCategory.COMPLAINT.toCode(), ConditionCategory.COMPLAINT.getDisplay())));

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
		Patient readPatient = client.read().resource(Patient.class).withId(TestDatabaseInitializer.getPatient().getId())
				.execute();

		// search by patient and category
		Bundle results = client.search().forResource(Condition.class)
				.where(Condition.SUBJECT.hasId(readPatient.getId()))
				.and(Condition.CATEGORY.exactly().code("problem-list-item")).returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		Condition condition = (Condition) entries.get(0).getResource();
		assertNotNull(condition);

		assertEquals("Patient/" + TestDatabaseInitializer.getPatient().getId(), condition.getSubject().getReference());
		assertNotNull(condition.getCategory());
		assertNotNull(condition.getCategory().get(0).getCoding());
		assertFalse(condition.getCategory().get(0).getCoding().isEmpty());
		assertEquals("problem-list-item", condition.getCategory().get(0).getCoding().get(0).getCode());
	}
}
