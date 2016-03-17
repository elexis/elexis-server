package info.elexis.server.core.connector.elexis.locking;

import java.util.Optional;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockResponse;

public interface ILockService {
	/**
	 * All or none
	 * 
	 * @param objectIds
	 * @param userId
	 * @return
	 */

	public LockResponse acquireLock(LockInfo lockInfo);

	/**
	 * To be implemented by a {@link ILockServiceContributor} only!
	 * 
	 * @param lockInfos
	 * @param lockServiceContributorClass
	 *            to skip
	 * @return
	 */
	public LockResponse acquireLock(LockInfo lockInfos,
			Class<? extends ILockServiceContributor> lockServiceContributorClass);

	/**
	 * 
	 * @param objectIds
	 * @param userId
	 * @return
	 */
	public LockResponse releaseLock(LockInfo lockInfos);

	public LockResponse releaseLock(LockInfo lockInfos,
			Class<? extends ILockServiceContributor> lockServiceContributorClass);

	public boolean isLocked(String storeToString);

	public Optional<LockInfo> getLockInfo(String storeToString);
}
