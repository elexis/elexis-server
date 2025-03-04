package es.fhir.rest.core.websocket.r4;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.LoggerFactory;

import jakarta.websocket.server.ServerEndpointConfig;

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
			e.printStackTrace();
			LoggerFactory.getLogger(getClass()).error("Error starting Jetty for WebSocket", e);
		}
	}

	@Deactivate
	public void deactivate() throws Exception {
		server.stop();
	}

	public static Server newServer(int port) {
		// see https://jetty.org/docs/jetty/12/programming-guide/server/websocket.html
		Server server = new Server(port);

		ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		handler.setContextPath("/");
		server.setHandler(handler);

		// TODO AccessFilter?
		JakartaWebSocketServletContainerInitializer.configure(handler, (servletContext, container) -> {
			container.setDefaultMaxTextMessageBufferSize(128 * 1024);
			container.addEndpoint(ServerEndpointConfig.Builder.create(SubscriptionWebSocket.class, ALIAS).build());
		});

		return server;
	}

}
