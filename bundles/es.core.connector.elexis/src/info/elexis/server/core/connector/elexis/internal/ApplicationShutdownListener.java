package info.elexis.server.core.connector.elexis.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.connector.elexis.internal.services.locking.LockService;
import info.elexis.server.core.contrib.IApplicationShutdownListener;

public class ApplicationShutdownListener implements IApplicationShutdownListener {

	private Logger log = LoggerFactory.getLogger(ApplicationShutdownListener.class);

	@Override
	public String performShutdown(boolean forced) {

		int locks = LockService.getAllLockInfo().size();
		if (locks > 0) {
			if (forced) {
				log.warn("Clearing {} lock(s).", locks);
				LockService.clearAllLocks();
			} else {
				return locks + " lock(s) held.";
			}
		}

		return null;
	}

}
