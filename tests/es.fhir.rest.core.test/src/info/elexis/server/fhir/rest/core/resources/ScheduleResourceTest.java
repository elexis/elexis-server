package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Schedule;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.agenda.AreaType;
import ch.elexis.core.services.holder.AppointmentServiceHolder;
import info.elexis.server.fhir.rest.core.test.AllTests;
import info.elexis.server.fhir.rest.core.test.FhirUtil;

public class ScheduleTest {
	
	private static IGenericClient client;
	
	private static IUser user;
	
	@BeforeClass
	public static void setupClass() throws IOException, SQLException{
		client = FhirUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
		
		user = AllTests.getTestDatabaseInitializer().getUser();
		AppointmentServiceHolder.get().setAreaType("Arzt 1", AreaType.CONTACT,
			user.getAssignedContact().getId());
	}
	
	@Test
	public void testRetrievAllScheduleAreas(){
		Bundle results =
			client.search().forResource(Schedule.class).returnBundle(Bundle.class).execute();
		for (BundleEntryComponent entry : results.getEntry()) {
			Schedule schedule = (Schedule) entry.getResource();
			System.out.println(schedule.getId() + " " + schedule.getComment());
		}
		assertEquals(7, results.getEntry().size());
	}
	
	// RELOADDASHBOARDVIEW_2
	@Test
	public void searchUserSchedule(){
		// without userid
		String searchUrl = "Schedule?_query=user-schedule&userid=" + user.getId();
		// no fluent approach found to query by queryname
		Bundle results = client.search().byUrl(searchUrl).returnBundle(Bundle.class).execute();
		
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		Schedule schedule = (Schedule) entries.get(0).getResource();
		Identifier elexisIdentifier = schedule.getIdentifier().get(0);
		assertEquals(CodingSystem.ELEXIS_AGENDA_AREA_ID.getSystem(), elexisIdentifier.getSystem());
		assertEquals("Arzt 1", elexisIdentifier.getValue());
	}
	
}
