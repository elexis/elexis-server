package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent;
import org.hl7.fhir.r4.model.Appointment.ParticipantRequired;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Schedule;
import org.hl7.fhir.r4.model.Slot;
import org.hl7.fhir.r4.model.Slot.SlotStatus;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.time.TimeUtil;
import info.elexis.server.fhir.rest.core.test.AllTests;
import info.elexis.server.fhir.rest.core.test.FhirUtil;

public class AppointmentResourceProviderTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		AllTests.getTestDatabaseInitializer().initializeAgendaTable();

		client = FhirUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
	}

	@Test
	public void directLoadAppointmentInContactAssignedArea() {
		Appointment appointment = client.read().resource(Appointment.class).withId("D3eeb6d9398e45029093231").execute();
		assertNotNull(appointment);
		assertEquals(1482908400000l, appointment.getStart().getTime());
		assertEquals(1482940800000l, appointment.getEnd().getTime());
		assertEquals(540, appointment.getMinutesDuration());

		List<AppointmentParticipantComponent> participants = appointment.getParticipant();
		assertEquals(1, participants.size());
		assertEquals(ParticipantRequired.REQUIRED, participants.get(0).getRequired());

		Practitioner practitioner = client.read().resource(Practitioner.class)
				.withId(participants.get(0).getActor().getReferenceElement()).execute();
		assertNotNull(practitioner);
		assertEquals("Nachname", practitioner.getName().get(0).getFamily());

		List<Reference> slotReference = appointment.getSlot();
		assertNotNull(slotReference);

		Slot slot = client.read().resource(Slot.class).withId(slotReference.get(0).getReferenceElement()).execute();
		assertNotNull(slot);
		assertEquals(SlotStatus.BUSY, slot.getStatus());

		Schedule schedule = client.read().resource(Schedule.class).withId(slot.getSchedule().getReferenceElement())
				.execute();
		assertNotNull(schedule);
//		practitioner = client.read().resource(Practitioner.class)
//				.withId(schedule.getActor().get(0).getReferenceElement()).execute();
//		assertEquals("Nachname", practitioner.getName().get(0).getFamily());
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

	@Test
	public void searchByMultipleChainedSlotSchedule() {
		// Praxis
		String searchUrl = "Appointment?slot.schedule=5495888F8AAE05023409B5CF853BBBCE";
		Bundle results = client.search().byUrl(searchUrl).returnBundle(Bundle.class).execute();
		assertEquals(3, results.getEntry().size());

		// Praxis and Arzt 1
		searchUrl = "Appointment?slot.schedule=5495888F8AAE05023409B5CF853BBBCE,81DF9C49C3DAE56BD7DE851682E2B34F";
		results = client.search().byUrl(searchUrl).returnBundle(Bundle.class).execute();
		assertEquals(6, results.getEntry().size());
	}

	@Test
	public void testSearchByDateParamsAndSlot() {
		Bundle results = client.search().forResource(Appointment.class)
				.where(Appointment.DATE.afterOrEquals().day("2016-12-01"))
				.and(Appointment.DATE.before().day("2016-12-30"))
				.and(Appointment.SLOT.hasChainedProperty(Slot.SCHEDULE.hasId("68A891B86923DD1740345627DBB92C9F"))).returnBundle(Bundle.class)
				.execute();
		assertEquals(2, results.getEntry().size());
		for (BundleEntryComponent entry : results.getEntry()) {
			Appointment appointment = (Appointment) entry.getResource();
			assertTrue(appointment.getParticipant().get(0).getActor().getReference().startsWith("Patient/"));
			assertNull(appointment.getParticipant().get(0).getActorTarget());
		}
	}

	@Test
	public void testSearchByDateParamsAndSlotAndIncludePatientReference() {
		Bundle results = client.search().forResource(Appointment.class)
				.where(Appointment.DATE.afterOrEquals().day("2016-12-01"))
				.and(Appointment.DATE.before().day("2016-12-30"))
				.and(Appointment.SLOT.hasChainedProperty(Slot.SCHEDULE.hasId("68A891B86923DD1740345627DBB92C9F")))
				.include(Appointment.INCLUDE_PATIENT.asNonRecursive()).returnBundle(Bundle.class).execute();
		System.out.println(results);
		assertEquals(4, results.getEntry().size());

		System.out.println(FhirUtil.serializeToString(results));
	}

	@Test
	public void updateVersionedAppointment() {
		Appointment result = client.read().resource(Appointment.class).withId("Af322a333db4daf37093177").execute();
		assertEquals("1483615525111", result.getMeta().getVersionId());

		// backend modify appointment
		IAppointment appointment = AllTests.getModelService().load("Af322a333db4daf37093177", IAppointment.class)
				.orElseThrow();
		appointment.setSchedule("Kiril Checkov");
		AllTests.getModelService().save(appointment);

		// try to update now old version
		ResourceVersionConflictException ex = null;
		try {
			client.update().resource(result).execute();
		} catch (ResourceVersionConflictException rvce) {
			ex = rvce;
		}
		assertNotNull(ex);
		assertEquals(409, ex.getStatusCode());

		result = client.read().resource(Appointment.class).withId("Af322a333db4daf37093177").execute();
		Date now = new Date();
		result.setStart(now);
		result.setEnd(new Date(now.getTime() + 5 * 60 * 1000));

		client.update().resource(result).execute();

		AllTests.getModelService().refresh(appointment);

		LocalDateTime startTime = appointment.getStartTime();
		LocalDateTime localDateTime = TimeUtil.toLocalDateTime(now);

		boolean isEqualInSecondPrecision = startTime.truncatedTo(ChronoUnit.MINUTES)
				.equals(localDateTime.truncatedTo(ChronoUnit.MINUTES));
		assertTrue(isEqualInSecondPrecision);

	}

}
