package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import ca.uhn.fhir.rest.client.method.HttpGetClientInvocation;
import ca.uhn.fhir.rest.client.method.MethodUtil;
import ch.elexis.core.hapi.fhir.FhirUtil;

public class CORSTest {
	
	private static IGenericClient client;
	
	@BeforeClass
	public static void setupClass() throws IOException, SQLException{
		client = FhirUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
	}
	
	@Test
	public void testCorsIsAvailable() throws IOException{
		HttpGetClientInvocation invocation =
			MethodUtil.createConformanceInvocation(client.getFhirContext());
		invocation.addHeader("Origin", "localhost"); // to enable CORS Filter
		IHttpRequest httpRequest =
			invocation.asHttpRequest(client.getServerBase(), null, null, true);
		IHttpResponse response = httpRequest.execute();
		assertEquals(response.getStatus(), 200, response.getStatus());
		Map<String, List<String>> allHeaders = response.getAllHeaders();
		List<String> list = allHeaders.get("access-control-allow-origin");
		assertNotNull(allHeaders.toString(), list);
		assertEquals("*", list.get(0));
	}
	
}
