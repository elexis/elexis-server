package info.elexis.server.core.connector.elexis.locking;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IStoreToStringService;
import ch.elexis.core.utils.OsgiServiceUtil;
import info.elexis.server.core.connector.elexis.services.LockService;

public enum LockServiceInstance implements ILockService {
		
		INSTANCE;
	
	private Logger log = LoggerFactory.getLogger(LockServiceInstance.class);
	
	private LockService ls = new LockService();
	
	private IStoreToStringService storeToStringService =
		OsgiServiceUtil.getService(IStoreToStringService.class).get();
	
	@Override
	public boolean isLocked(LockInfo lockInfo){
		return ls.isLocked(lockInfo);
	}
	
	@Override
	public Optional<LockInfo> getLockInfo(String objectId){
		return ls.getLockInfo(objectId);
	}
	
	@Override
	public LockResponse acquireLock(LockInfo lockInfos){
		return ls.acquireLock(lockInfos);
	}
	
	@Override
	public LockResponse releaseLock(LockInfo lockInfos){
		return ls.releaseLock(lockInfos);
	}
	
	@Override
	public LockResponse acquireLock(LockInfo lockInfo,
		Class<? extends ILockServiceContributor> lockServiceContributorClass){
		return ls.acquireLock(lockInfo, lockServiceContributorClass);
	}
	
	@Override
	public LockResponse acquireLockBlocking(LockInfo lockInfo, int timeout){
		return ls.acquireLockBlocking(lockInfo, timeout);
	}
	
	@Override
	public LockResponse releaseLock(LockInfo lockInfo,
		Class<? extends ILockServiceContributor> lockServiceContributorClass){
		return ls.releaseLock(lockInfo, lockServiceContributorClass);
	}
	
	@Override
	public LockResponse releaseLockBlocking(LockInfo lockInfos,
		Class<? extends ILockServiceContributor> lockServiceContributorClass, int timeout){
		return ls.releaseLockBlocking(lockInfos, lockServiceContributorClass, timeout);
	}
	
	@Override
	public LockResponse acquireLockBlocking(LockInfo lockInfo,
		Class<? extends ILockServiceContributor> lockServiceContributorClass, int timeout){
		return ls.acquireLockBlocking(lockInfo, lockServiceContributorClass, timeout);
	}
	
	/**
	 * Convenience method to acquire a blocking lock for an {@link AbstractDBObjectIdDeleted}
	 * 
	 * @param lobj
	 * @param timeout
	 * @return
	 */
	public Optional<LockInfo> acquireLockBlocking(Identifiable lobj, int timeout){
		Optional<String> sts = storeToStringService.storeToString(lobj);
		if (sts.isPresent()) {
			log.trace("Trying to acquire lock blocking ({}sec) for [{}].", timeout, sts.get());
			LockInfo ls = new LockInfo(sts.get(), LockService.getElexisserveragentuser(),
				LockService.getSystemuuid());
			LockResponse lr = acquireLockBlocking(ls, timeout);
			if (!lr.isOk()) {
				log.error("Failed acquiring lock for [{}]",
					lobj.getClass().getName() + "@" + lobj.getId(), new Throwable("Diagnosis"));
				return Optional.empty();
			}
			return Optional.of(ls);
		}
		log.warn("Could not resolve storeToString for [{}]", lobj);
		return Optional.empty();
	}
	
	public Optional<LockInfo> acquireLockBlocking(Identifiable lobj){
		return acquireLockBlocking(lobj, 15);
	}
	
}
