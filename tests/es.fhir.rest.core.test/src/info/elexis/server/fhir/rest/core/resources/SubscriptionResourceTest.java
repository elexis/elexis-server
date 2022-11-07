package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Hashtable;

import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Subscription.SubscriptionChannelComponent;
import org.hl7.fhir.r4.model.Subscription.SubscriptionChannelType;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.services.IAppointmentService;
import ch.elexis.core.utils.OsgiServiceUtil;
import info.elexis.server.fhir.rest.core.test.AllTests;
import info.elexis.server.fhir.rest.core.test.FhirUtil;

public class SubscriptionResourceTest {

	private static IGenericClient client;

	@BeforeClass
	public static void beforeClass() throws IOException, SQLException {

		client = FhirUtil.getGenericClient(AllTests.GENERIC_CLIENT_URL);
		assertNotNull(client);

		Hashtable<String, Object> props = new Hashtable<>();
		FrameworkUtil.getBundle(SubscriptionResourceTest.class).getBundleContext().registerService(
				SubscriptionResourceTestEndpointProvider.class.getName(),
				new SubscriptionResourceTestEndpointProvider(), props);
	}

	@Test
	public void createReceiveDeleteGenericAppointmentSubscription() throws InterruptedException {

		// init subscription object
		Subscription appointmentSubscription = new Subscription();
		appointmentSubscription.setReason("Unit-Test");
		appointmentSubscription.setCriteria("Appointment");

		SubscriptionChannelComponent scc = new SubscriptionChannelComponent();
		scc.setEndpoint("http://localhost:8380/services/subscription-test-endpoint/post");
		// https://www.hl7.org/fhir/subscription.html#2.46.8.1
		scc.setType(SubscriptionChannelType.RESTHOOK);
		appointmentSubscription.setChannel(scc);

		// register subscription
		MethodOutcome execute = client.create().resource(appointmentSubscription).execute();
		assertTrue(execute.getCreated());
		assertNotNull(execute.getId());
		assertEquals("Subscription", execute.getId().getResourceType());
		appointmentSubscription = (Subscription) execute.getResource();

		assertEquals(0, SubscriptionResourceTestEndpointProvider.getPostCallCounter());

		// create an appointment
		IAppointmentService appointmentService = OsgiServiceUtil.getService(IAppointmentService.class).orElse(null);
		assertNotNull(appointmentService);
		IAppointment appointment = AllTests.getModelService().create(IAppointment.class);
		appointment.setSchedule("test-area");
		appointment.setStartTime(LocalDateTime.now());
		appointment.setSubjectOrPatient("test-appointment-generic");
		appointment.setEndTime(LocalDateTime.now().plusMinutes(5));
		AllTests.getModelService().save(appointment);

		// wait for the subscription call to happen
		for (int i = 0; i < 1000; i++) {
			int callCounter = SubscriptionResourceTestEndpointProvider.getPostCallCounter();
			if (callCounter > 0) {
				continue;
			}
			Thread.sleep(200);
		}

		assertTrue(SubscriptionResourceTestEndpointProvider.getPostCallCounter() > 0);

		execute = client.delete().resource(appointmentSubscription).execute();
	}

	@Test
	public void createReceiveDeletePayloadUpdateAppointmentSubscription() throws InterruptedException {
		// init subscription object
		Subscription appointmentSubscription = new Subscription();
		appointmentSubscription.setReason("Unit-Test");
		appointmentSubscription.setCriteria("Appointment");

		SubscriptionChannelComponent scc = new SubscriptionChannelComponent();
		scc.setEndpoint("http://localhost:8380/services/subscription-test-endpoint");
		// https://www.hl7.org/fhir/subscription.html#2.46.8.1
		scc.setPayload("application/fhir+json");
		scc.setType(SubscriptionChannelType.RESTHOOK);
		scc.setHeader(Collections.singletonList(new StringType("headerkey: headervalue")));
		appointmentSubscription.setChannel(scc);

		// register subscription
		MethodOutcome execute = client.create().resource(appointmentSubscription).execute();
		assertTrue(execute.getCreated());
		assertNotNull(execute.getId());
		assertEquals("Subscription", execute.getId().getResourceType());
		appointmentSubscription = (Subscription) execute.getResource();

		assertEquals(0, SubscriptionResourceTestEndpointProvider.getPutCallCounter());
		assertEquals(0, SubscriptionResourceTestEndpointProvider.getDeleteCallCounter());

		// create an appointment
		IAppointmentService appointmentService = OsgiServiceUtil.getService(IAppointmentService.class).orElse(null);
		assertNotNull(appointmentService);
		IAppointment appointment = AllTests.getModelService().create(IAppointment.class);
		appointment.setSchedule("test-area");
		appointment.setStartTime(LocalDateTime.now());
		appointment.setSubjectOrPatient("test-appointment");
		appointment.setEndTime(LocalDateTime.now().plusMinutes(5));
		AllTests.getModelService().save(appointment);

		// wait for the subscription call to happen
		for (int i = 0; i < 1000; i++) {
			int putCallCounter = SubscriptionResourceTestEndpointProvider.getPutCallCounter();
			if (putCallCounter > 0) {
				continue;
			}
			Thread.sleep(200);
		}

		assertTrue(SubscriptionResourceTestEndpointProvider.getPutCallCounter() > 0);

		AllTests.getModelService().delete(appointment);

		// wait for the subscription call to happen
		for (int i = 0; i < 1000; i++) {
			int putCallCounter = SubscriptionResourceTestEndpointProvider.getDeleteCallCounter();
			if (putCallCounter > 0) {
				continue;
			}
			Thread.sleep(200);
		}

		assertTrue(SubscriptionResourceTestEndpointProvider.getDeleteCallCounter() > 0);

		execute = client.delete().resource(appointmentSubscription).execute();
	}

}
