package info.elexis.jaxrs.service.internal.test;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.junit.Test;

import ch.elexis.core.utils.OsgiServiceUtil;
import info.elexis.jaxrs.service.internal.JaxRsJerseyServlet;

public class JaxRsJerseyServletTest {

	@Test
	public void start() {
		JaxRsJerseyServlet servlet = OsgiServiceUtil.getServiceWait(JaxRsJerseyServlet.class, 5000).orElseThrow();

		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://localhost:8380" + JaxRsJerseyServlet.ALIAS);
		MockResource resource = WebResourceFactory.newResource(MockResource.class, target);
		assertEquals("postblabla", resource.postContent("blabla"));
		assertEquals(MockResourceImpl.getMockElement(), resource.getJson());
		assertEquals(MockResourceImpl.getMockElement(), resource.getXml());
	}

}
