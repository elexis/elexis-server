package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.sql.SQLException;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Schedule;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import info.elexis.server.fhir.rest.core.test.AllTests;
import info.elexis.server.fhir.rest.core.test.FhirUtil;

public class ScheduleResourceTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		AllTests.getTestDatabaseInitializer().initializeAgendaTable();

		client = FhirUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
	}

	@Test
	public void testRetrievAllScheduleAreas() {
		Bundle results = client.search().forResource(Schedule.class).returnBundle(Bundle.class).execute();
		for (BundleEntryComponent entry : results.getEntry()) {
			Schedule schedule = (Schedule) entry.getResource();
			System.out.println(schedule.getId() + " " + schedule.getText().getDivAsString());
		}
		assertEquals(7, results.getEntry().size());
	}

//	// RELOADDASHBOARDVIEW_2
//	@Test
//	public void searchUserSchedule(){
//		// without userid
//		String searchUrl = "Schedule?_query=user-schedule&userid=" + user.getId();
//		// no fluent approach found to query by queryname
//		Bundle results = client.search().byUrl(searchUrl).returnBundle(Bundle.class).execute();
//		
//		assertNotNull(results);
//		List<BundleEntryComponent> entries = results.getEntry();
//		assertFalse(entries.isEmpty());
//		Schedule schedule = (Schedule) entries.get(0).getResource();
//		Identifier elexisIdentifier = schedule.getIdentifier().get(0);
//		assertEquals(CodingSystem.ELEXIS_AGENDA_AREA_ID.getSystem(), elexisIdentifier.getSystem());
//		assertEquals("Arzt 1", elexisIdentifier.getValue());
//	}

}
