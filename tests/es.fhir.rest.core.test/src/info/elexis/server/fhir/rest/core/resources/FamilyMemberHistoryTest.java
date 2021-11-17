package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.FamilyMemberHistory;
import org.hl7.fhir.r4.model.Narrative;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ch.elexis.core.findings.IFamilyMemberHistory;
import info.elexis.server.fhir.rest.core.test.AllTests;
import info.elexis.server.fhir.rest.core.test.FhirUtil;

public class FamilyMemberHistoryTest {
	
	private static IGenericClient client;
	
	@BeforeClass
	public static void setupClass() throws IOException, SQLException{
		AllTests.getTestDatabaseInitializer().initializePatient();
		
		
		client = FhirUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
		
		IFamilyMemberHistory familyMemberHistory =
			AllTests.getFindingsService().create(IFamilyMemberHistory.class);
		familyMemberHistory.setText("Family Member History test 1");
		familyMemberHistory.setPatientId(AllTests.getTestDatabaseInitializer().getPatient().getId());
		
		AllTests.getFindingsService().saveFinding(familyMemberHistory);

	}
	
	@AfterClass
	public static void afterClass(){
		AllTests.deleteAllFindings();
	}
	
	@Test
	public void testFamilyMemberHistory(){
		// search for encounter
		Bundle results = client.search().forResource(FamilyMemberHistory.class)
			.where(FamilyMemberHistory.PATIENT.hasId(AllTests.getTestDatabaseInitializer().getPatient().getId()))
			.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
	}
	
	@Test
	public void createFamilyMemberHistory(){
		FamilyMemberHistory familyMemberHistory = new FamilyMemberHistory();
		
		Narrative narrative = new Narrative();
		String divEncodedText = "Test\nText".replaceAll("(\r\n|\r|\n)", "<br />");
		narrative.setDivAsString(divEncodedText);
		familyMemberHistory.setText(narrative);
		
		MethodOutcome outcome = client.create().resource(familyMemberHistory).execute();
		assertNotNull(outcome);
		assertTrue(outcome.getCreated());
		assertNotNull(outcome.getId());
		
		FamilyMemberHistory readFamilyMemberHistory =
			client.read().resource(FamilyMemberHistory.class).withId(outcome.getId()).execute();
		assertNotNull(readFamilyMemberHistory);
		assertEquals(outcome.getId().getIdPart(),
			readFamilyMemberHistory.getIdElement().getIdPart());
	}
	
}
