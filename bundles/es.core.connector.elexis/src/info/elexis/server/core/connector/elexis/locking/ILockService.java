package info.elexis.server.core.connector.elexis.locking;

import java.util.Optional;

import info.elexis.server.elexis.common.types.LockInfo;


public interface ILockService {
	/**
	 * All or none
	 * 
	 * @param objectIds
	 * @param userId
	 * @return
	 */

	public boolean acquireLock(LockInfo lockInfo);
	
	/**
	 * To be implemented by a {@link ILockServiceContributor} only!
	 * @param lockInfos
	 * @param lockServiceContributorClass to skip
	 * @return
	 */
	public boolean acquireLock(LockInfo lockInfos, Class<? extends ILockServiceContributor> lockServiceContributorClass);

	/**
	 * 
	 * @param objectIds
	 * @param userId
	 * @return
	 */
	public boolean releaseLock(LockInfo lockInfos);

	public boolean releaseLock(LockInfo lockInfos, Class<? extends ILockServiceContributor> lockServiceContributorClass);
	
	public boolean isLocked(String storeToString);

	public Optional<LockInfo> getLockInfo(String storeToString);
}
