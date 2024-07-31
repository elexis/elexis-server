package es.fhir.rest.core.websocket.r4;

import org.eclipse.jetty.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.websocket.server.JettyWebSocketServletFactory;

public class SubscriptionWebSocketServlet extends JettyWebSocketServlet {

	private static final long serialVersionUID = -552313430556081361L;

	@Override
	protected void configure(JettyWebSocketServletFactory factory) {
		factory.register(SubscriptionWebSocket.class);
	}

}
