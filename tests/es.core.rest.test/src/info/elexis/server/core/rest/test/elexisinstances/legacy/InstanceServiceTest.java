package info.elexis.server.core.rest.test.elexisinstances.legacy;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import ch.elexis.core.common.InstanceStatus;
import ch.elexis.core.server.IInstanceService;
import info.elexis.server.core.rest.test.AllTests;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InstanceServiceTest {

	private static IInstanceService instanceService;

	@BeforeClass
	public static void beforeClass() {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(AllTests.REST_URL);
		instanceService = WebResourceFactory.newResource(IInstanceService.class, target);
	}

	@Test
	public void _01_testUpdateStatus() {
		InstanceStatus instanceStatus = new InstanceStatus();
		instanceStatus.setState(InstanceStatus.STATE.ACTIVE);
		instanceStatus.setUuid("testUuid");
		instanceStatus.setVersion("testVersion");
		instanceStatus.setOperatingSystem(System.getProperty("os.name") + "/" + System.getProperty("os.version") + "/"
				+ System.getProperty("os.arch") + "/J" + System.getProperty("java.version"));
		instanceStatus.setIdentifier("testIdentifier");
		instanceStatus.setActiveUser("testActiveUser");

		instanceService.updateStatus(instanceStatus);
	}

	// getStatus does not correctly work
}
