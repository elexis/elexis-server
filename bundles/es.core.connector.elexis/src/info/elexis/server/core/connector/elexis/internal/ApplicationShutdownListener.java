package info.elexis.server.core.connector.elexis.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.connector.elexis.services.LockService;
import info.elexis.server.core.contrib.IApplicationShutdownListener;

public class ApplicationShutdownListener implements IApplicationShutdownListener {

	private static Logger log = LoggerFactory.getLogger(Activator.class);

	@Override
	public String performShutdown(boolean forced) {

		int locks = LockService.getAllLockInfo().size();
		if (locks > 0) {
			if (forced) {
				log.warn("Clearing " + locks + " lock(s).");
				LockService.clearAllLocks();
			} else {
				return locks + " lock(s) held.";
			}
		}

		return null;
	}

}
