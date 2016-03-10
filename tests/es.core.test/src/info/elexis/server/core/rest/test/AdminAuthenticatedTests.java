package info.elexis.server.core.rest.test;

import static info.elexis.server.core.constants.RestPathConstants.*;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static com.eclipsesource.restfuse.Assert.*;

import com.eclipsesource.restfuse.Destination;
import com.eclipsesource.restfuse.HttpJUnitRunner;
import com.eclipsesource.restfuse.Method;
import com.eclipsesource.restfuse.Response;
import com.eclipsesource.restfuse.annotation.Context;
import com.eclipsesource.restfuse.annotation.Header;
import com.eclipsesource.restfuse.annotation.HttpTest;

import info.elexis.server.core.rest.test.common.TestHelper;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(HttpJUnitRunner.class)
public class AdminAuthenticatedTests {

	private static String adminSessionId = null;

	@Rule
	public Destination SERVICE = getDestination();

	@Context
	private Response response;

	private Destination getDestination() {
		Destination destination = new Destination(this, "http://localhost:8380/services/");
		if (AdminAuthenticatedTests.adminSessionId != null) {
			destination.getRequestContext().addHeader("sessionId", adminSessionId);
		}
		return destination;
	}

	// initializes the admin session, has to be executed as first test
	@HttpTest(method = Method.GET, path = LOGIN + "/admin", headers = { @Header(name = "encoded", value = "admin") })
	public void testAA_authenticateAgainstDefaultAuthorizingRealm() {
		String body = response.getBody();
		assertOk(response);
		AdminAuthenticatedTests.adminSessionId = body;
	}
	
//	@HttpTest(method = Method.GET, path = LOGIN + "/dz", headers = { @Header(name = "encoded", value = "dz") })
//	public void testAB_authenticateAgainstElexisAuthorizingRealm() {
//		String body = response.getBody();
//		assertOk(response);
//		AdminAuthenticatedTests.adminSessionId = body;
//	}
//	
	@HttpTest(method = Method.POST, path = ELEXIS_CONNECTION, content = "invalidStuff")
	public void testSetElexisConnectionPostInvalidObject() {
		assertBadRequest(response);
	}
	
	@HttpTest(method = Method.POST, path = ELEXIS_CONNECTION, content = TestHelper.DBCONNECTION_XML_STRING)
	public void testSetElexisConnection() {
		assertOk(response);
	}

	// will stop the system, has to be executed as last test
	// does not work for headless plugin tests
//	@HttpTest(method = Method.GET, path = HALT)
//	public void testZZ_HaltSystemWithAuthentication() {
//		assertOk(response);
//	}
}
