package info.elexis.jaxrs.service.internal;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import jakarta.servlet.annotation.WebServlet;

@WebServlet(urlPatterns = { "/*" })
public class JaxRsJerseyServlet extends ServletContainer {

	private static final long serialVersionUID = -131084922708281927L;

	public JaxRsJerseyServlet() {
		super(new MyResourceConfig());
	}

	// #TODO move
	// info.elexis.server.core.security.internal.JaxRsServletConfiguration
	// #TODO provide endpoint to list all routes
	// #TODO swagger annotations endpoint

	private static class MyResourceConfig extends ResourceConfig {
		public MyResourceConfig() {
			JaxRsJerseyServletCollector.getJaxrsServletSet().forEach(this::register);
		}
	}

}
