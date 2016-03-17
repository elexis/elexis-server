package info.elexis.server.core.connector.elexis.locking;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockResponse;

public interface ILockServiceContributor {

	LockResponse acquireLock(LockInfo lockInfos);

	LockResponse releaseLock(LockInfo lockInfos);

}
