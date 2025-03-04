package info.elexis.jaxrs.service.internal.test;

import static org.junit.Assert.assertEquals;

import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.junit.Test;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;

public class JaxRsJerseyServletTest {

	@Test
	public void start() {
//		JaxRsJerseyServlet servlet = OsgiServiceUtil.getServiceWait(JaxRsJerseyServlet.class, 5000).orElseThrow();

		Client client = ClientBuilder.newClient();
//		WebTarget target = client.target("http://localhost:8380" + JaxRsJerseyServlet.ALIAS);
		WebTarget target = client.target("http://localhost:8380/services");
		MockResource resource = WebResourceFactory.newResource(MockResource.class, target);
		assertEquals("postblabla", resource.postContent("blabla"));
		assertEquals(MockResourceImpl.getMockElement(), resource.getJson());
		assertEquals(MockResourceImpl.getMockElement(), resource.getXml());
	}

}
