package info.elexis.server.core.connector.elexis.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;

@Component(property = { EventConstants.EVENT_TOPIC + "=" + ElexisEventTopics.BASE + "*" })
public class ElexisEventHandler implements EventHandler {

	private Logger log = LoggerFactory.getLogger(ElexisEventHandler.class);
	
	@Override
	public void handleEvent(Event event) {
		log.trace("Incoming event [{}] propertyNames [{}]", event.getTopic(), event.getPropertyNames());
	}

}
