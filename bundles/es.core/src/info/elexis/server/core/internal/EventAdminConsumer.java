package info.elexis.server.core.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.EventAdmin;

@Component(immediate = true)
public class EventAdminConsumer {

	private static EventAdmin eventAdmin;
	
	@Reference(
            service = EventAdmin.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.STATIC,
            unbind = "unsetEventAdmin"
    )
	protected synchronized void setEventAdmin(EventAdmin ea) {
		EventAdminConsumer.eventAdmin = ea;
	}
	
	protected synchronized void unsetEventAdmin(EventAdmin ea) {
		EventAdminConsumer.eventAdmin = null;
	}

	public static EventAdmin getEventAdmin() {
		return eventAdmin;
	}

}
