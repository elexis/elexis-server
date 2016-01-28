package info.elexis.server.core.connector.elexis.jpa.test;

import static com.eclipsesource.restfuse.Assert.assertBadRequest;
import static com.eclipsesource.restfuse.Assert.assertOk;
import static info.elexis.server.core.constants.RestPathConstants.ELEXIS_CONNECTION;
import static info.elexis.server.core.constants.RestPathConstants.LOGIN;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

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
public class ElexisDBAuthenticatedTest {

	private static String adminSessionId = null;

	@Rule
	public Destination SERVICE = getDestination();

	@Context
	private Response response;

	private Destination getDestination() {
		Destination destination = new Destination(this, "http://localhost:8380/services/");
		if (ElexisDBAuthenticatedTest.adminSessionId != null) {
			destination.getRequestContext().addHeader("sessionId", adminSessionId);
		}
		return destination;
	}
	
	@HttpTest(method = Method.GET, path = LOGIN + "/dz", headers = { @Header(name = "encoded", value = "dz") })
	public void testAA_authenticateAgainstElexisAuthorizingRealm() {
		String body = response.getBody();
		assertOk(response);
		ElexisDBAuthenticatedTest.adminSessionId = body;
	}
	
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
