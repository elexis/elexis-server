package es.fhir.rest.core.websocket.r4;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class SubscriptionWebSocketConnections {

	private static final Map<String, SubscriptionWebSocket> activeWebSockets = Collections
			.synchronizedMap(new HashMap<>());

	public static void put(String subscriptionId, SubscriptionWebSocket subscriptionWebSocket) {
		activeWebSockets.put(subscriptionId, subscriptionWebSocket);
	}

	public static void remove(String subscriptionId) {
		if (subscriptionId != null) {
			activeWebSockets.remove(subscriptionId);
		}
	}

	/**
	 * Send a ping message according to
	 * http://hl7.org/fhir/R4B/subscription.html#2.46.8.2
	 * 
	 * @param id
	 * @return
	 */
	public static IStatus sendPing(String id) {
		SubscriptionWebSocket subscriptionWebSocket = activeWebSockets.get(id);
		if (subscriptionWebSocket != null) {
			return subscriptionWebSocket.sendPing();
		}
		return Status.error("SubscriptionWebSockt for id [" + id + "] not found");
	}

}
