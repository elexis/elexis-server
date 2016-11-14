package info.elexis.server.core.connector.elexis.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEvent;

@Component(property = { EventConstants.EVENT_TOPIC + "=" + ElexisEvent.EVENT_BASE + "*" })
public class ElexisEventHandler implements EventHandler {

	private Logger log = LoggerFactory.getLogger(ElexisEventHandler.class);
	
	@Override
	public void handleEvent(Event event) {
		log.debug("Incoming event [{}]", event.getTopic());
	}

}
