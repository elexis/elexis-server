package es.fhir.rest.core.websocket.r4;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.fhir.rest.core.resources.SubscriptionResourceProvider;

/**
 * Implements the FHIR Subscription WebSocket endpoint
 * 
 * @see http://hl7.org/fhir/R4B/subscription.html#2.46.8.2
 */
@WebSocket
public class SubscriptionWebSocket {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private String subscriptionId;

	private Session session;
	private RemoteEndpoint remote;

	@OnWebSocketConnect
	public void onWebSocketConnect(Session session) {
		// client initiates connection
		this.session = session;
		this.remote = this.session.getRemote();
	}

	@OnWebSocketMessage
	public void onWebSocketText(String message) {
		if (this.session != null && this.session.isOpen() && this.remote != null && message != null) {
			if (message.startsWith("bind") && message.length() > 10 && !isInitialized()) {
				// connection to subscription
				String proposedId = message.substring("bind".length() + 1);
				log.info("bind {} {}", proposedId, session);

				boolean isValid = SubscriptionResourceProvider.isValidSubscriptionId(proposedId);
				if (isValid) {
					subscriptionId = proposedId;
					SubscriptionWebSocketConnections.put(subscriptionId, this);
					this.remote.sendString("bound " + subscriptionId, WriteCallback.NOOP);
				} else {
					// invalid id submitted, we close the connection
					this.remote.sendString("invalid " + proposedId, WriteCallback.NOOP);
					this.session.close();
				}

			} else if (message.startsWith("ping")) {
				this.remote.sendString("pong " + message, WriteCallback.NOOP);
			}
		}

	}

	@OnWebSocketClose
	public void onWebSocketClose(int statusCode, String reason) {
		SubscriptionWebSocketConnections.remove(subscriptionId);
		this.session = null;
		this.remote = null;
	}

	@OnWebSocketError
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
			remote.sendString("ping " + subscriptionId);
			return Status.OK_STATUS;
		} catch (IOException e) {
			return Status.error("Error sending ping", e);
		}
	}

	private boolean isInitialized() {
		return subscriptionId != null;
	}

	public RemoteEndpoint getRemote() {
		return remote;
	}

}
