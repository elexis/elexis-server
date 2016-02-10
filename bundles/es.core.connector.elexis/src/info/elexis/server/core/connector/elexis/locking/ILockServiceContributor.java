package info.elexis.server.core.connector.elexis.locking;

import java.util.List;

import info.elexis.server.elexis.common.types.LockInfo;

public interface ILockServiceContributor {

	boolean acquireLocks(List<LockInfo> lockInfos);

	boolean releaseLocks(List<LockInfo> lockInfos);

}
