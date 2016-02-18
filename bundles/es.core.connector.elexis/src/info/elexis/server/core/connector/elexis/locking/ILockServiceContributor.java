package info.elexis.server.core.connector.elexis.locking;

import info.elexis.server.elexis.common.types.LockInfo;

public interface ILockServiceContributor {

	boolean acquireLock(LockInfo lockInfos);

	boolean releaseLock(LockInfo lockInfos);

}
