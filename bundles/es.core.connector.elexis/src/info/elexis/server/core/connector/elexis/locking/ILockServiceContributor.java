package info.elexis.server.core.connector.elexis.locking;

import info.elexis.server.elexis.common.types.LockInfo;
import info.elexis.server.elexis.common.types.LockResponse;

public interface ILockServiceContributor {

	LockResponse acquireLock(LockInfo lockInfos);

	LockResponse releaseLock(LockInfo lockInfos);

}
