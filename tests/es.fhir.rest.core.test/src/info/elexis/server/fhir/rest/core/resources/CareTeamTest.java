package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.sql.SQLException;

import org.hl7.fhir.r4.model.CareTeam;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import info.elexis.server.fhir.rest.core.test.AllTests;
import info.elexis.server.fhir.rest.core.test.FhirUtil;

public class CareTeamTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		AllTests.getTestDatabaseInitializer().initializeUserGroup();

		client = FhirUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
	}

	@Test
	public void readCareTeam() {
		CareTeam careTeam = client.read().resource(CareTeam.class)
				.withId(AllTests.getTestDatabaseInitializer().getUserGroup().getId()).execute();
		assertNotNull(careTeam);
		assertFalse(careTeam.getParticipant().isEmpty());
	}
}
