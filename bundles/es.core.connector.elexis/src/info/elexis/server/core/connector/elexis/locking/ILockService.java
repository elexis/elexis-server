package info.elexis.server.core.connector.elexis.locking;

import java.util.List;
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

	public boolean acquireLocks(List<LockInfo> lockInfos);

	/**
	 * 
	 * @param objectIds
	 * @param userId
	 * @return
	 */

	public boolean releaseLocks(List<LockInfo> lockInfos);

	public boolean isLocked(String storeToString);

	public Optional<LockInfo> getLockInfo(String storeToString);
}
