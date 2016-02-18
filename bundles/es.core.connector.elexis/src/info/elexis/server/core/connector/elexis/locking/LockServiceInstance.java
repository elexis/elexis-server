package info.elexis.server.core.connector.elexis.locking;

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
	public boolean acquireLock(LockInfo lockInfos) {
		return ls.acquireLock(lockInfos);
	}

	@Override
	public boolean releaseLock(LockInfo lockInfos) {
		return ls.releaseLock(lockInfos);
	}

	@Override
	public boolean acquireLock(LockInfo lockInfo,
			Class<? extends ILockServiceContributor> lockServiceContributorClass) {
		return ls.acquireLock(lockInfo, lockServiceContributorClass);
	}

	@Override
	public boolean releaseLock(LockInfo lockInfo,
			Class<? extends ILockServiceContributor> lockServiceContributorClass) {
		return ls.releaseLock(lockInfo, lockServiceContributorClass);
	}



}
