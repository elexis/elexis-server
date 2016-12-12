package info.elexis.server.core.connector.elexis.locking;

import java.util.Optional;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockResponse;

public interface ILockService {

	/**
	 * Request a lock for a given {@link LockInfo} referenced object
	 * (non-blocking)
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
}
