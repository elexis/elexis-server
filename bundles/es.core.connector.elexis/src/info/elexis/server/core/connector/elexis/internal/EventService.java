package info.elexis.server.core.connector.elexis.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEvent;
import ch.elexis.core.common.ElexisEventTopics;
import info.elexis.server.core.connector.elexis.jpa.ElexisTypeMap;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.services.LockService;

@Component
public class EventService {

	private static Logger log = LoggerFactory.getLogger(EventService.class);

	private static EventAdmin eventAdmin;

	@Reference(service = EventAdmin.class, cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "unsetEventAdmin")
	protected synchronized void setEventAdmin(EventAdmin ea) {
		EventService.eventAdmin = ea;
	}

	protected synchronized void unsetEventAdmin(EventAdmin ea) {
		EventService.eventAdmin = null;
	}

	public static void postEvent(ElexisEvent elexisEvent) {
		if (elexisEvent == null || elexisEvent.getTopic() == null) {
			return;
		}
		String topic = elexisEvent.getTopic();
		if (!topic.startsWith(ElexisEventTopics.BASE)) {
			topic = ElexisEventTopics.BASE + topic;
		}
		Event event = new Event(topic, elexisEvent.getProperties());
		if (EventService.eventAdmin != null) {
			EventService.eventAdmin.sendEvent(event);
		} else {
			log.warn("Received post event, but EventAdmin is null");
		}
	}

	public static void postCreationEvent(AbstractDBObjectIdDeleted entity, String userId) {
		ElexisEvent ee = new ElexisEvent();
		ee.setTopic(ElexisEventTopics.PERSISTENCE_EVENT_CREATE);
		ee.getProperties().put(ElexisEventTopics.PROPKEY_ID, entity.getId());
		ee.getProperties().put(ElexisEventTopics.PROPKEY_CLASS, ElexisTypeMap.getKeyForObject(entity));
		if (userId == null) {
			userId = LockService.elexisServerAgentUser;
		}
		ee.getProperties().put(ElexisEventTopics.PROPKEY_USER, userId);
		postEvent(ee);
	}

}
