package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Slot;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ch.elexis.core.model.IAppointment;
import info.elexis.server.fhir.rest.core.test.AllTests;
import info.elexis.server.fhir.rest.core.test.FhirUtil;

public class SlotResourceTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		AllTests.getTestDatabaseInitializer().initializeAgendaTable();

		client = FhirUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
	}

	@Test
	public void updateScheduleStartEndTime() {

		Optional<IAppointment> load = AllTests.getModelService().load("se4bf3d3fa547776b093261", IAppointment.class);
		assertTrue(load.isPresent());
		assertTrue(load.get().getSchedule().startsWith("Thomas"));
		assertNotEquals(LocalDate.now(), load.get().getStartTime().toLocalDate());
		assertNotEquals(LocalDate.now(), load.get().getEndTime().toLocalDate());

		Slot slot = client.read().resource(Slot.class).withId("se4bf3d3fa547776b093261").execute();
		assertEquals("se4bf3d3fa547776b093261", slot.getIdElement().getIdPart());
		assertEquals("68A891B86923DD1740345627DBB92C9F", slot.getSchedule().getReferenceElement().getIdPart());

		Reference scheduleReference = new Reference("Schedule/D071C7B8E492AA966222B6B6F08B1B45");
		slot.setSchedule(scheduleReference);
		Date now = new Date();
		slot.setStart(now);
		now.setTime(slot.getStart().getTime() + 1000 * 60 * 5);
		slot.setEnd(now);

		client.update().resource(slot).execute();
		
		load = AllTests.getModelService().load("se4bf3d3fa547776b093261", IAppointment.class, true, true);
		assertTrue(load.isPresent());
		assertTrue(load.get().getSchedule(), load.get().getSchedule().startsWith("Julian"));
		assertEquals(LocalDate.now(), load.get().getStartTime().toLocalDate());
		assertEquals(LocalDate.now(), load.get().getEndTime().toLocalDate());
	}

}
