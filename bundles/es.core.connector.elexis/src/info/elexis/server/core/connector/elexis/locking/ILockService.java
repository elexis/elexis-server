package info.elexis.server.core.connector.elexis.locking;

import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.LoggerFactory;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import info.elexis.server.core.SystemPropertyConstants;

/**
 * Server side implementation of the lock service, which allows for {@link ILockServiceContributor}s
 *
 */
public interface ILockService {
	
	/**
	 * Request a lock for a given {@link LockInfo} referenced object (non-blocking)
	 * 
	 * @param lockInfo
	 * @return
	 */
	public LockResponse acquireLock(LockInfo lockInfo);
	
	/**
	 * Tries to acquire a lock for a given number of seconds
	 * 
	 * @param lockInfo
	 * @param timeout
	 *            in seconds
	 * @return
	 */
	public LockResponse acquireLockBlocking(LockInfo lockInfo, int timeout);
	
	/**
	 * To be implemented by a {@link ILockServiceContributor} only!
	 * 
	 * @param lockInfos
	 * @param lockServiceContributorClass
	 *            contributor to skip
	 * @return
	 */
	public LockResponse acquireLock(LockInfo lockInfos,
		Class<? extends ILockServiceContributor> lockServiceContributorClass);
	
	/**
	 * Tries to acquire a lock for a given number of seconds
	 * 
	 * @param lockInfo
	 * @param lockServiceContributorClass
	 * @param timeout
	 *            in seconds
	 * @return
	 */
	public LockResponse acquireLockBlocking(LockInfo lockInfo,
		Class<? extends ILockServiceContributor> lockServiceContributorClass, int timeout);
	
	
	default public LockResponse acquireLockBlocking(Object po, int timeout,
		IProgressMonitor monitor){
		if (po instanceof Identifiable) {
			Identifiable lobj = (Identifiable) po;
			String sts = StoreToStringServiceHolder.getStoreToString(lobj);
			LockInfo ls = new LockInfo(sts, SystemAgentUser.ELEXISSERVER_AGENTUSER,
				SystemPropertyConstants.getStationId());
			LoggerFactory.getLogger(getClass())
				.trace("Trying to acquire lock blocking ({}sec) for [{}].", timeout, sts);
			LockResponse lr = acquireLockBlocking(ls, timeout);
			if (!lr.isOk()) {
				LoggerFactory.getLogger(getClass()).error("Failed acquiring lock for [{}]",
					lobj.getClass().getName() + "@" + lobj.getId(), new Throwable("Diagnosis"));
			}
			return lr;
		}
		return LockResponse.ERROR;
	}
	
	/**
	 * Convenience method to acquire a blocking lock for an {@link AbstractDBObjectIdDeleted}
	 * 
	 * @param lobj
	 * @param timeout
	 * @return
	 */
	default public Optional<LockInfo> acquireLockBlocking(Identifiable lobj, int timeout){
		String sts = StoreToStringServiceHolder.getStoreToString(lobj);
		if (sts != null) {
			LoggerFactory.getLogger(getClass())
				.trace("Trying to acquire lock blocking ({}sec) for [{}].", timeout, sts);
			LockInfo lockInfo = new LockInfo(sts, SystemAgentUser.ELEXISSERVER_AGENTUSER, getSystemUuid());
			LockResponse lr = acquireLockBlocking(lockInfo, timeout);
			if (!lr.isOk()) {
				LoggerFactory.getLogger(getClass()).error("Failed acquiring lock for [{}]",
					lobj.getClass().getName() + "@" + lobj.getId(), new Throwable("Diagnosis"));
				return Optional.empty();
			}
			return Optional.of(lockInfo);
		}
		LoggerFactory.getLogger(getClass()).warn("Could not resolve storeToString for [{}]", lobj);
		return Optional.empty();
	}
	
	/**
	 * 
	 * @param lockInfos
	 * @return
	 */
	public LockResponse releaseLock(LockInfo lockInfos);
	
	/**
	 * 
	 * @param lockInfos
	 * @param lockServiceContributorClass
	 *            contributor to skip
	 * @return
	 */
	public LockResponse releaseLock(LockInfo lockInfos,
		Class<? extends ILockServiceContributor> lockServiceContributorClass);
	
	/**
	 * Tries to release a lock for a given number of seconds
	 * 
	 * @param lockInfos
	 * @param lockServiceContributorClass
	 * @param timeout
	 * @return
	 */
	public LockResponse releaseLockBlocking(LockInfo lockInfos,
		Class<? extends ILockServiceContributor> lockServiceContributorClass, int timeout);
	
	/**
	 * 
	 * @param lockInfo
	 * @return
	 */
	public boolean isLocked(LockInfo lockInfo);
	
	/**
	 * 
	 * @param storeToString
	 * @return
	 */
	public Optional<LockInfo> getLockInfo(String storeToString);
	
	public String getSystemUuid();
	
}
