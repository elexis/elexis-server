package es.fhir.rest.core.websocket.r4;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jetty.websocket.api.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.fhir.rest.core.resources.SubscriptionResourceProvider;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/**
 * Implements the FHIR Subscription WebSocket endpoint
 * 
 * @see http://hl7.org/fhir/R4B/subscription.html#2.46.8.2
 */
@ServerEndpoint(value = "/websocketR4")
public class SubscriptionWebSocket {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private String subscriptionId;

	private Session session;

	@OnOpen
	public void onWebSocketConnect(Session session) {
		// client initiates connection
		this.session = session;
	}

	@OnMessage
	public void onWebSocketText(String message) {
		if (this.session != null && this.session.isOpen() && this.session != null && message != null) {
			try {
				if (message.startsWith("bind") && message.length() > 10 && !isInitialized()) {
					// connection to subscription
					String proposedId = message.substring("bind".length() + 1);
					log.info("bind {} {}", proposedId, session);

					boolean isValid = SubscriptionResourceProvider.isValidSubscriptionId(proposedId);
					if (isValid) {
						subscriptionId = proposedId;
						SubscriptionWebSocketConnections.put(subscriptionId, this);
						this.session.getBasicRemote().sendText("bound " + subscriptionId);
					} else {
						// invalid id submitted, we close the connection
						this.session.getBasicRemote().sendText("invalid " + proposedId);
						this.session.close();
					}

				} else if (message.startsWith("ping")) {
					this.session.getBasicRemote().sendText("pong ");
				}
			} catch (IOException e) {
				log.warn("Error sending message", e);
				e.printStackTrace();
			}

		}

	}

	@OnClose
	public void onWebSocketClose(Session session, CloseReason reason) {
		SubscriptionWebSocketConnections.remove(subscriptionId);
		this.session = null;
	}

	@OnError
	public void onWebSocketError(Throwable cause) {
		log.warn("[{}] WebSocket Error {}", subscriptionId, session, cause);
		SubscriptionWebSocketConnections.remove(subscriptionId);
	}

	/**
	 * Send a ping message according to
	 * http://hl7.org/fhir/R4B/subscription.html#2.46.8.2
	 * 
	 * @param id
	 * @return
	 */
	public IStatus sendPing() {
		try {
			session.getBasicRemote().sendText("ping " + subscriptionId);
		} catch (IOException e) {
			return Status.error(subscriptionId, e);
		}
		return Status.OK_STATUS;
	}

	private boolean isInitialized() {
		return subscriptionId != null;
	}

}
