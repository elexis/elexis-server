package info.elexis.server.core.rest.test;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.model.Statement;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;

import static com.eclipsesource.restfuse.Assert.*;

import static info.elexis.server.core.constants.RestPathConstants.*;

import com.eclipsesource.restfuse.Destination;
import com.eclipsesource.restfuse.HttpJUnitRunner;
import com.eclipsesource.restfuse.MediaType;
import com.eclipsesource.restfuse.Method;
import com.eclipsesource.restfuse.Response;
import com.eclipsesource.restfuse.annotation.Context;
import com.eclipsesource.restfuse.annotation.HttpTest;

import com.eclipsesource.restfuse.annotation.Header;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(HttpJUnitRunner.class)
public class UnauthenticatedTests {

	@Rule
	public Destination SERVICE = new Destination(this, "http://localhost:8380/services/");

	@Context
	private Response response;

	@BeforeClass
	public static void initialize() throws Exception {
		Thread.sleep(1000);
	}

	@HttpTest(method = Method.GET, path = "/")
	public void testGetStatus() {
		String body = response.getBody();
		assertOk(response);
		assertEquals(MediaType.TEXT_PLAIN, response.getType());
		assertEquals(body.startsWith("Uptime"), true);
	}

	@HttpTest(method = Method.GET, path = LOGIN + "/admin", headers = { @Header(name = "encoded", value = "admin") })
	public void testAdminLogin() {
		assertOk(response);
		assertEquals(MediaType.TEXT_PLAIN, response.getType());
	}

	@HttpTest(method = Method.GET, path = HALT)
	public void testHaltWithoutAuthentication() {
		assertForbidden(response);
	}

	@HttpTest(method = Method.GET, path = RESTART)
	public void testRestartWithoutAuthentication() {
		assertForbidden(response);
	}

	@HttpTest(method = Method.GET, path = SCHEDULER, type = MediaType.APPLICATION_XML)
	public void testSchedulerInfo() {
		assertOk(response);
	}

	@HttpTest(method = Method.GET, path = ELEXIS_CONNECTION, type = MediaType.APPLICATION_XML)
	public void testGetElexisDBConnection() {
		assertOk(response);
	}
}
