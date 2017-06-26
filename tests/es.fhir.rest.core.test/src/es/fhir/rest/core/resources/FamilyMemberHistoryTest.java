package es.fhir.rest.core.resources;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.FamilyMemberHistory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.IGenericClient;
import ch.elexis.core.findings.IFamilyMemberHistory;
import ch.elexis.core.findings.IFindingsFactory;
import ch.elexis.core.findings.util.ModelUtil;
import es.fhir.rest.core.test.AllTests;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;

public class FamilyMemberHistoryTest {
	
	private static IGenericClient client;
	
	@BeforeClass
	public static void setupClass() throws IOException, SQLException{
		TestDatabaseInitializer initializer = new TestDatabaseInitializer();
		initializer.initializePatient();
		
		
		client = ModelUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
		
		IFindingsFactory iFindingsFactory = AllTests.getFindingsService().getFindingsFactory();
		IFamilyMemberHistory familyMemberHistory = iFindingsFactory.createFamilyMemberHistory();
		familyMemberHistory.setText("Family Member History test 1");
		familyMemberHistory.setPatientId(TestDatabaseInitializer.getPatient().getId());
		
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
			.where(FamilyMemberHistory.PATIENT.hasId(TestDatabaseInitializer.getPatient().getId()))
			.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
	}
	
}
