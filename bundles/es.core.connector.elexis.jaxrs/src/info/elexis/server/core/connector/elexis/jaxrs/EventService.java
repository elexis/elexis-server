package info.elexis.server.core.connector.elexis.jaxrs;

import javax.ws.rs.core.Response;

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
import ch.elexis.core.server.IEventService;

@Component
public class EventService implements IEventService {

	private static Logger log = LoggerFactory.getLogger(EventService.class);

	private static EventAdmin eventAdmin;

	@Reference(service = EventAdmin.class, cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "unsetEventAdmin")
	protected synchronized void setEventAdmin(EventAdmin ea) {
		EventService.eventAdmin = ea;
	}

	protected synchronized void unsetEventAdmin(EventAdmin ea) {
		EventService.eventAdmin = null;
	}

	@Override
	public Response postEvent(ElexisEvent elexisEvent) {
		if (elexisEvent == null || elexisEvent.getTopic() == null) {
			return Response.serverError().build();
		}
		String topic = elexisEvent.getTopic();
		if (!topic.startsWith(ElexisEventTopics.TOPIC_BASE)) {
			topic = ElexisEventTopics.TOPIC_BASE + topic;
		}
		Event event = new Event(topic, elexisEvent.getProperties());
		if (EventService.eventAdmin != null) {
			EventService.eventAdmin.postEvent(event);
		} else {
			log.warn("Received post event, but EventAdmin is null");
		}
		return Response.ok().build();
	}

}
