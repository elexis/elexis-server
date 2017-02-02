package info.elexis.server.core.connector.elexis.jpa.test.eventHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.StringConstants;

@Component(property = { EventConstants.EVENT_TOPIC + "=" + ElexisEventTopics.BASE + "*" })
public class TestEventHandler implements EventHandler {

	private static String failure;
	private static Event newestEvent;
	private static Set<String> singleOccurenceSet = new HashSet<>();
	private static ConcurrentLinkedDeque<Event> eventQueue =  new ConcurrentLinkedDeque<Event>();

	@Override
	public void handleEvent(Event event) {
		newestEvent = event;

		String topic = event.getTopic();
		if (topic == null) {
			failure = "Event with topic [null]";
		}
		if (ElexisEventTopics.PERSISTENCE_EVENT_CREATE.equals(topic)) {
			String key = event.getProperty(ElexisEventTopics.PROPKEY_CLASS).toString() + StringConstants.DOUBLECOLON
					+ event.getProperty(ElexisEventTopics.PROPKEY_ID).toString();
			if (singleOccurenceSet.contains(key)) {
				failure = "Double event occurence of key [" + key + "]";
			}
			singleOccurenceSet.add(key);
		}

		eventQueue.add(event);
	}

	public synchronized static Event waitforEvent() throws IllegalStateException {
		int count = 0;
		while (newestEvent == null) {
			if (failure != null) {
				throw new IllegalStateException(failure + " [" + newestEvent + "]");
			}
			try {
				Thread.sleep(10);
				count++;
				if (count > 10) {
					return null;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Event retEvent = newestEvent;
		newestEvent = null;
		return retEvent;
	}

	public static Event pollEvent() {
		return eventQueue.pollLast();
	}

	public static void assertCreateEvent(Event event, String typeVerrechnet) {
		if (ElexisEventTopics.PERSISTENCE_EVENT_CREATE.equals(event.getTopic())) {
			if (typeVerrechnet.equalsIgnoreCase((String) event.getProperty(ElexisEventTopics.PROPKEY_CLASS))) {
				return;
			}
		}
		throw new AssertionError("Not a create event or wrong class [" + event + "]");
	}

	public static void clearEventList() {
		eventQueue.clear();
	}

}
