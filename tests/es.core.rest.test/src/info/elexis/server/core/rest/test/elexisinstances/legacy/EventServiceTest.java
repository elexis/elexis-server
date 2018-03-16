package info.elexis.server.core.rest.test.elexisinstances.legacy;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.eclipsesource.jaxrs.consumer.ConsumerFactory;

import ch.elexis.core.common.InstanceStatus;
import ch.elexis.core.server.IEventService;
import info.elexis.server.core.rest.test.AllTests;
import info.elexis.server.core.rest.test.elexisinstances.ElexisServerClientConfig;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EventServiceTest {

	private static IEventService eventService;

	@BeforeClass
	public static void beforeClass() {
		eventService = ConsumerFactory.createConsumer(AllTests.REST_URL, new ElexisServerClientConfig(),
				IEventService.class);
	}

	@Test
	public void _01_testUpdateStatus() {
		InstanceStatus instanceStatus = new InstanceStatus();

	}

}
