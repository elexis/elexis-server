package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Hashtable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Subscription.SubscriptionChannelComponent;
import org.hl7.fhir.r4.model.Subscription.SubscriptionChannelType;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.services.IAppointmentService;
import ch.elexis.core.services.holder.ConfigServiceHolder;
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

		ConfigServiceHolder.get().set("agenda/bereiche", "test-area");
	}

	@Test
	@Ignore
	public void resthook_createReceiveDeleteGenericAppointmentSubscription() throws InterruptedException {

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

		Subscription _updatedSubscription = client.read().resource(Subscription.class)
				.withId(appointmentSubscription.getId()).execute();
		assertEquals(appointment.getLastupdate(), (Long) _updatedSubscription.getMeta().getLastUpdated().getTime());

		execute = client.delete().resource(appointmentSubscription).execute();
	}

//	private int activeSubscriptionCount() {
//	return client.search().forResource(Subscription.class).where(Subscription.STATUS.exactly().code("active"))
//			.cacheControl(new CacheControlDirective().setNoCache(true)).returnBundle(Bundle.class).execute()
//			.getEntry().size();
//}

	@Test
	@Ignore
	public void resthook_createReceiveDeletePayloadUpdateAppointmentSubscription() throws InterruptedException {
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

		// create an appointment (we check the incoming subscription order lastupdate
		// value)
		assertNotNull(appointmentService);
		IAppointment _appointment = AllTests.getModelService().create(IAppointment.class);
		_appointment.setSchedule("test-area");
		_appointment.setStartTime(LocalDateTime.now().plusMinutes(10));
		_appointment.setSubjectOrPatient("test-appointment-2");
		_appointment.setEndTime(LocalDateTime.now().plusMinutes(15));
		AllTests.getModelService().save(_appointment);

		// wait for the subscription call to happen
		for (int i = 0; i < 1000; i++) {
			int putCallCounter = SubscriptionResourceTestEndpointProvider.getPutCallCounter();
			if (putCallCounter > 0) {
				continue;
			}
			Thread.sleep(200);
		}

		assertTrue(SubscriptionResourceTestEndpointProvider.getPutCallCounter() > 0);

		Subscription _updatedSubscription = client.read().resource(Subscription.class)
				.withId(appointmentSubscription.getId()).execute();
		assertEquals(_appointment.getLastupdate(), (Long) _updatedSubscription.getMeta().getLastUpdated().getTime());

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

	@Test
	public void websocket_createReceiveDeleteGenericAppointmentSubscription() throws Exception {

		// init subscription object
		Subscription appointmentSubscription = new Subscription();
		appointmentSubscription.setReason("Unit-Test-Websocket");
		appointmentSubscription.setCriteria("Appointment");
		SubscriptionChannelComponent scc = new SubscriptionChannelComponent();
		scc.setType(SubscriptionChannelType.WEBSOCKET);
		appointmentSubscription.setChannel(scc);

		// register subscription
		MethodOutcome execute = client.create().resource(appointmentSubscription).execute();
		assertTrue(execute.getCreated());

		WebSocketClient websocketClient = new WebSocketClient();
		websocketClient.start();

		URI uri = URI.create("ws://localhost:8381/websocketR4");

		SubscriptionWebsocketListener subscriptionWebsocketListener = new SubscriptionWebsocketListener();
		CompletableFuture<Session> fut = websocketClient.connect(subscriptionWebsocketListener, uri);
		Session session = fut.get(5, TimeUnit.SECONDS);
		session.sendText("bind " + execute.getId().getIdPart(), null);

		String msg = subscriptionWebsocketListener.messageQueue.poll(5, TimeUnit.SECONDS);
		assertEquals("bound " + execute.getId().getIdPart(), msg);

		session.close(StatusCode.NORMAL, "exit", Callback.NOOP);
		websocketClient.stop();
	}

	public static class SubscriptionWebsocketListener implements Session.Listener {

		private static final Logger LOG = LoggerFactory.getLogger(SubscriptionWebsocketListener.class);
		private final LinkedBlockingDeque<String> messageQueue = new LinkedBlockingDeque<>();
		private final CountDownLatch closeLatch = new CountDownLatch(1);

		@Override
		public void onWebSocketText(String message) {
			LOG.info("Text Message [{}]", message);
			messageQueue.offer(message);
		}
	}

}
