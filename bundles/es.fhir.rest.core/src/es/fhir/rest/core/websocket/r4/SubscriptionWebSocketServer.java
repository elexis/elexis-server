package es.fhir.rest.core.websocket.r4;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.LoggerFactory;

@Component(service = {}, immediate = true)
public class SubscriptionWebSocketServer {

	public static final String ALIAS = "/websocketR4";

	private Server server;

	@Activate
	public void activate() {
		server = SubscriptionWebSocketServer.newServer(8381);
		try {
			server.start();
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass()).error("Error starting Jetty for WebSocket", e);
		}
	}

	@Deactivate
	public void deactivate() throws Exception {
		server.stop();
	}

	public static Server newServer(int port) {
		Server server = new Server(port);

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);

		// TODO AccessFilter?

		JettyWebSocketServletContainerInitializer.configure(context, null);
		ServletHolder wsHolder = new ServletHolder("websocketR4", new SubscriptionWebSocketServlet());
		context.addServlet(wsHolder, "/websocketR4");

		return server;
	}

}
