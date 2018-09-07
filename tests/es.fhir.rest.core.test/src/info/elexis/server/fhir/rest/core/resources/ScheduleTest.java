package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.sql.SQLException;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Schedule;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import info.elexis.server.hapi.fhir.FhirUtil;

public class ScheduleTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		client = FhirUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
	}

	@Test
	public void testRetrievAllScheduleAreas() {
		Bundle results = client.search().forResource(Schedule.class).returnBundle(Bundle.class).execute();
		for (BundleEntryComponent entry : results.getEntry()) {
			Schedule schedule = (Schedule) entry.getResource();
			System.out.println(schedule.getId() + " " + schedule.getComment());
		}
		assertEquals(7, results.getEntry().size());
	}
}
