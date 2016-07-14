package info.elexis.server.core.contrib;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationShutdownRegistrar {

	private static Logger log = LoggerFactory.getLogger(ApplicationShutdownRegistrar.class);
	
	static Set<IApplicationShutdownListener> listeners = Collections
			.synchronizedSet(new HashSet<IApplicationShutdownListener>());

	public static void addShutdownListener(IApplicationShutdownListener ias) {
		log.debug("Adding application shutdown listener ["+ias.getClass().getName()+"]");
		listeners.add(ias);
	}

	public static void removeShutdownListener(IApplicationShutdownListener ias) {
		log.debug("Removing application shutdown listener ["+ias.getClass().getName()+"]");
		listeners.remove(ias);
	}

	public static Set<IApplicationShutdownListener> getApplicationShutdownListeners() {
		return new HashSet<IApplicationShutdownListener>(listeners);
	}

}
