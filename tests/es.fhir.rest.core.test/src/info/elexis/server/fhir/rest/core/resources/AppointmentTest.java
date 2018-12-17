package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import org.hl7.fhir.dstu3.model.Appointment;
import org.hl7.fhir.dstu3.model.Appointment.AppointmentParticipantComponent;
import org.hl7.fhir.dstu3.model.Appointment.AppointmentStatus;
import org.hl7.fhir.dstu3.model.Appointment.ParticipantRequired;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Schedule;
import org.hl7.fhir.dstu3.model.Slot;
import org.hl7.fhir.dstu3.model.Slot.SlotStatus;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.IGenericClient;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;

public class AppointmentTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		TestDatabaseInitializer initializer = new TestDatabaseInitializer();
		initializer.initializeAgendaTable();

		client = ModelUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
	}

	@Test
	public void testDirectLoadAppointmentInContactAssignedArea() {
		Appointment result = client.read().resource(Appointment.class).withId("Af322a333db4daf37093177").execute();
		assertNotNull(result);
		assertEquals(AppointmentStatus.BOOKED, result.getStatus());
		assertEquals(1484116200000l, result.getStart().getTime());
		assertEquals(1484121600000l, result.getEnd().getTime());
		assertEquals(90, result.getMinutesDuration());

		List<AppointmentParticipantComponent> participants = result.getParticipant();
		assertEquals(1, participants.size());
		assertEquals(ParticipantRequired.REQUIRED, participants.get(0).getRequired());

		Practitioner practitioner = client.read().resource(Practitioner.class)
				.withId(participants.get(0).getActor().getReferenceElement()).execute();
		assertNotNull(practitioner);
		assertEquals("Nachname", practitioner.getName().get(0).getFamily());

		List<Reference> slotReference = result.getSlot();
		assertNotNull(slotReference);

		Slot slot = client.read().resource(Slot.class).withId(slotReference.get(0).getReferenceElement()).execute();
		assertNotNull(slot);
		assertEquals(SlotStatus.BUSY, slot.getStatus());

		Schedule schedule = client.read().resource(Schedule.class).withId(slot.getSchedule().getReferenceElement())
				.execute();
		assertNotNull(schedule);
		practitioner = client.read().resource(Practitioner.class)
				.withId(schedule.getActor().get(0).getReferenceElement()).execute();
		assertEquals("Nachname", practitioner.getName().get(0).getFamily());
	}

	@Test
	public void testSearchByDateParams() {
		Calendar cal = Calendar.getInstance();
		cal.set(2017, 0, 11, 0, 0, 0);

		Bundle results = client.search().forResource(Appointment.class)
				.where(Appointment.DATE.afterOrEquals().day(cal.getTime())).returnBundle(Bundle.class).execute();
		assertEquals(1, results.getEntry().size());

		results = client.search().forResource(Appointment.class)
				.where(Appointment.DATE.afterOrEquals().day("2014-05-14"))
				.and(Appointment.DATE.before().day("2016-11-15")).returnBundle(Bundle.class).execute();
		assertEquals(8, results.getEntry().size());
	}

}
