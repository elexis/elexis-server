package info.elexis.server.core.connector.elexis.locking;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockResponse;

/**
 * Contribute to the locking process
 */
public interface ILockServiceContributor {
	
	LockResponse acquireLock(LockInfo lockInfos);
	
	LockResponse releaseLock(LockInfo lockInfos);
	
}
