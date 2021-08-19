package info.elexis.server.core.connector.elexis.internal.services;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class LogEventHandler implements EventHandler {
	
	private Logger log;
	private Marker marker;
	
	public LogEventHandler(){
		log = LoggerFactory.getLogger(getClass());
		marker = MarkerFactory.getMarker("ELEXIS-EVENT");
	}
	
	@Override
	public void handleEvent(Event event){
		log.info(marker, toLogString(event));
	}
	
	private String toLogString(Event event){
		StringBuilder sb = new StringBuilder();
		sb.append("("+event.getTopic()+") ");
		String[] propertyNames = event.getPropertyNames();
		for (String propertyName : propertyNames) {
			sb.append(propertyName+"="+event.getProperty(propertyName)+" ");
		}
		
		return sb.toString();
	}
}
