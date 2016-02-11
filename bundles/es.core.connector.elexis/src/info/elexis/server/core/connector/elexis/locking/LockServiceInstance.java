package info.elexis.server.core.connector.elexis.locking;

import java.util.List;
import java.util.Optional;

import info.elexis.server.core.connector.elexis.services.LockService;
import info.elexis.server.elexis.common.types.LockInfo;

public enum LockServiceInstance implements ILockService{

	INSTANCE;

	private LockService ls = new LockService();

	@Override
	public boolean isLocked(String objectId) {
		return ls.isLocked(objectId);
	}

	@Override
	public Optional<LockInfo> getLockInfo(String objectId) {
		return ls.getLockInfo(objectId);
	}

	@Override
	public boolean acquireLocks(List<LockInfo> lockInfos) {
		return ls.acquireLocks(lockInfos);
	}

	@Override
	public boolean releaseLocks(List<LockInfo> lockInfos) {
		return ls.releaseLocks(lockInfos);
	}

}
