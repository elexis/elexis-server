package info.elexis.server.core.connector.elexis.rest.legacy;

import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEvent;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.server.IEventService;
import info.elexis.jaxrs.service.JaxrsResource;

@Component
public class EventService implements IEventService, JaxrsResource {

	@Reference
	private EventAdmin eventAdmin;

	@Override
	public Response postEvent(ElexisEvent elexisEvent) {
		if (elexisEvent == null || elexisEvent.getTopic() == null) {
			return Response.serverError().build();
		}
		String topic = elexisEvent.getTopic();
		if (!topic.startsWith(ElexisEventTopics.BASE)) {
			topic = ElexisEventTopics.BASE + topic;
		}
		Event event = new Event("remote/" + topic, elexisEvent.getProperties());
		if (eventAdmin != null) {
			eventAdmin.postEvent(event);
		} else {
			LoggerFactory.getLogger(getClass()).warn("Received post event, but EventAdmin is null");
			return Response.serverError().build();
		}
		return Response.ok().build();
	}

}
