package info.elexis.server.core.rest.test.elexisinstances.legacy;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import ch.elexis.core.common.ElexisEvent;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.server.IEventService;
import info.elexis.server.core.rest.test.AllTests;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EventServiceTest {

	private static IEventService eventService;

	@BeforeClass
	public static void beforeClass() {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(AllTests.REST_URL);

		eventService = WebResourceFactory.newResource(IEventService.class, target);
	}

	@Test
	public void _01_testPostEvent() {
		ElexisEvent ee = new ElexisEvent();
		ee.setTopic(ElexisEventTopics.PERSISTENCE_EVENT_CREATE);
		ee.getProperties().put(ElexisEventTopics.PROPKEY_ID, "testObjectId");
		ee.getProperties().put(ElexisEventTopics.PROPKEY_CLASS, String.class.getName());
		eventService.postEvent(ee);
	}

}
