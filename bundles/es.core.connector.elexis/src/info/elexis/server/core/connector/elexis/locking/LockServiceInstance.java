package info.elexis.server.core.connector.elexis.locking;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockResponse;
import info.elexis.server.core.connector.elexis.jpa.StoreToStringService;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.services.LockService;

public enum LockServiceInstance implements ILockService {

	INSTANCE;

	private Logger log = LoggerFactory.getLogger(LockServiceInstance.class);

	private LockService ls = new LockService();

	@Override
	public boolean isLocked(LockInfo lockInfo) {
		return ls.isLocked(lockInfo);
	}

	@Override
	public Optional<LockInfo> getLockInfo(String objectId) {
		return ls.getLockInfo(objectId);
	}

	@Override
	public LockResponse acquireLock(LockInfo lockInfos) {
		return ls.acquireLock(lockInfos);
	}

	@Override
	public LockResponse releaseLock(LockInfo lockInfos) {
		return ls.releaseLock(lockInfos);
	}

	@Override
	public LockResponse acquireLock(LockInfo lockInfo,
			Class<? extends ILockServiceContributor> lockServiceContributorClass) {
		return ls.acquireLock(lockInfo, lockServiceContributorClass);
	}

	@Override
	public LockResponse acquireLockBlocking(LockInfo lockInfo, int timeout) {
		return ls.acquireLockBlocking(lockInfo, timeout);
	}
	
	@Override
	public LockResponse releaseLock(LockInfo lockInfo,
			Class<? extends ILockServiceContributor> lockServiceContributorClass) {
		return ls.releaseLock(lockInfo, lockServiceContributorClass);
	}
	
	@Override
	public LockResponse releaseLockBlocking(LockInfo lockInfos,
			Class<? extends ILockServiceContributor> lockServiceContributorClass, int timeout) {
		return ls.releaseLockBlocking(lockInfos, lockServiceContributorClass, timeout);
	}

	@Override
	public LockResponse acquireLockBlocking(LockInfo lockInfo,
			Class<? extends ILockServiceContributor> lockServiceContributorClass, int timeout) {
		return ls.acquireLockBlocking(lockInfo, lockServiceContributorClass, timeout);
	}

	/**
	 * Convenience method to acquire a blocking lock for an {@link AbstractDBObjectIdDeleted}
	 * @param lobj
	 * @param timeout
	 * @return
	 */
	public Optional<LockInfo> acquireLockBlocking(AbstractDBObjectIdDeleted lobj, int timeout) {
		String sts = StoreToStringService.storeToString(lobj);
		LockInfo ls = new LockInfo(sts, LockService.getElexisserveragentuser(), LockService.getSystemuuid());
		log.trace("Trying to acquire lock blocking ({}sec) for [{}].", timeout, sts);
		LockResponse lr = acquireLockBlocking(ls, timeout);
		if (!lr.isOk()) {
			log.error("Failed acquiring lock for " + lobj.getId() + ".");
			return Optional.empty();
		}
		return Optional.of(ls);
	}

	public Optional<LockInfo> acquireLockBlocking(AbstractDBObjectIdDeleted lobj) {
		return acquireLockBlocking(lobj, 15);
	}	

}
