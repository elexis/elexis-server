package info.elexis.server.core.connector.elexis.locking;

import java.util.List;
import java.util.Optional;

import info.elexis.server.core.connector.elexis.services.LockService;
import info.elexis.server.elexis.common.types.LockInfo;

public enum LockServiceInstance {

	INSTANCE;

	private LockService ls = new LockService();

	public boolean isLocked(String objectId) {
		return ls.isLocked(objectId);
	}

	public Optional<LockInfo> getLockInfo(String objectId) {
		return ls.getLockInfo(objectId);
	}

	public boolean acquireLocks(List<LockInfo> lockInfos) {
		return ls.acquireLocks(lockInfos);
	}

	public boolean releaseLocks(List<LockInfo> lockInfos) {
		return ls.releaseLocks(lockInfos);
	}

}
