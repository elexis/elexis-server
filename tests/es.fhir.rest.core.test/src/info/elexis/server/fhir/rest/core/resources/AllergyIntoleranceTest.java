package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.AllergyIntolerance.AllergyIntoleranceCategory;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Narrative;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ch.elexis.core.findings.IAllergyIntolerance;
import ch.elexis.core.findings.util.ModelUtil;
import info.elexis.server.fhir.rest.core.test.AllTests;
import info.elexis.server.fhir.rest.core.test.FhirUtil;

public class AllergyIntoleranceTest {
	
	private static IGenericClient client;
	
	@BeforeClass
	public static void setupClass() throws IOException, SQLException{
		AllTests.getTestDatabaseInitializer().initializePatient();
		
		
		client = FhirUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
		
		IAllergyIntolerance allergyIntolerance =
			AllTests.getFindingsService().create(IAllergyIntolerance.class);
		allergyIntolerance.setText("Allergy Intolerance test 1");
		allergyIntolerance.setPatientId(AllTests.getTestDatabaseInitializer().getPatient().getId());
		
		AllTests.getFindingsService().saveFinding(allergyIntolerance);
	}
	
	@AfterClass
	public static void afterClass(){
		AllTests.deleteAllFindings();
	}
	
	@Test
	public void testAllergyIntolerance(){
		Bundle results = client.search().forResource(AllergyIntolerance.class)
			.where(AllergyIntolerance.PATIENT.hasId(AllTests.getTestDatabaseInitializer().getPatient().getId()))
			.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
	}
	
	@Test
	public void createAllergyIntolerance(){
		AllergyIntolerance allergyIntolerance = new AllergyIntolerance();
		
		Narrative narrative = new Narrative();
		String divEncodedText = "Test\nText".replaceAll("(\r\n|\r|\n)", "<br />");
		narrative.setDivAsString(divEncodedText);
		allergyIntolerance.setText(narrative);
		
		MethodOutcome outcome = client.create().resource(allergyIntolerance).execute();
		assertNotNull(outcome);
		assertTrue(outcome.getCreated());
		assertNotNull(outcome.getId());
		
		AllergyIntolerance readAllergyIntolerance =
			client.read().resource(AllergyIntolerance.class).withId(outcome.getId()).execute();
		assertNotNull(readAllergyIntolerance);
		assertEquals(outcome.getId().getIdPart(),
			readAllergyIntolerance.getIdElement().getIdPart());
	}
	
	@Test
	public void updateAllergyIntolerance() {
		AllergyIntolerance allergyIntolerance = new AllergyIntolerance();

		Narrative narrative = new Narrative();
		String divEncodedText = "Test\nText".replaceAll("(\r\n|\r|\n)", "<br />");
		narrative.setDivAsString(divEncodedText);
		allergyIntolerance.setText(narrative);

		MethodOutcome outcome = client.create().resource(allergyIntolerance).execute();
		assertNotNull(outcome);
		assertTrue(outcome.getCreated());
		assertNotNull(outcome.getId());
		AllergyIntolerance readAllergyIntolerance = client.read().resource(AllergyIntolerance.class)
				.withId(outcome.getId()).execute();

		assertTrue(ModelUtil.getNarrativeAsString(readAllergyIntolerance.getText()).get().endsWith("Text"));
		assertFalse(readAllergyIntolerance.hasCategory(AllergyIntoleranceCategory.ENVIRONMENT));

		narrative = new Narrative();
		divEncodedText = "AllergyIntolerance\nTest\nUpdate".replaceAll("(\r\n|\r|\n)", "<br />");
		narrative.setDivAsString(divEncodedText);
		readAllergyIntolerance.setText(narrative);
		readAllergyIntolerance.addCategory(AllergyIntoleranceCategory.ENVIRONMENT);

		outcome = client.update().resource(readAllergyIntolerance).execute();
		assertNotNull(outcome);
		assertNotNull(outcome.getId());
		readAllergyIntolerance = client.read().resource(AllergyIntolerance.class).withId(outcome.getId()).execute();
		assertTrue(ModelUtil.getNarrativeAsString(readAllergyIntolerance.getText()).get().endsWith("Update"));
		assertTrue(readAllergyIntolerance.hasCategory(AllergyIntoleranceCategory.ENVIRONMENT));
	}

}
