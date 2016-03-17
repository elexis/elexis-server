package info.elexis.server.core.connector.elexis.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.lock.types.LockResponse.Status;
import info.elexis.server.core.connector.elexis.locking.ILockService;
import info.elexis.server.core.connector.elexis.locking.ILockServiceContributor;

@Component(service = {})
public class LockService implements ILockService {

	private static HashMap<String, LockInfo> locks = new HashMap<String, LockInfo>();
	private static List<ILockServiceContributor> contributors = new ArrayList<ILockServiceContributor>();

	private static Logger log = LoggerFactory.getLogger(LockService.class);

	/**
	 * A unique id for this instance of Elexis. Changes on every restart
	 */
	public static final UUID systemUuid = UUID.randomUUID();
	
	/**
	 * If true, a missing lock service contributor is a severe error which
	 * disable acquiring locks!
	 */
	private static final boolean failOnMissingLockServiceContributor;

	static {
		failOnMissingLockServiceContributor = (System.getProperty("acceptMissingLockServiceContributor") == null);
	}

	@Reference(service = ILockServiceContributor.class, cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "unsetLockServiceContributor")
	protected void setLockServiceContributor(ILockServiceContributor isc) {
		synchronized (contributors) {
			log.info("Binding lock service contributor " + isc.getClass());
			contributors.add(isc);
		}
	}

	protected void unsetLockServiceContributor(ILockServiceContributor isc) {
		synchronized (contributors) {
			log.info("Unbinding lock service contributor " + isc.getClass());
			contributors.remove(isc);
		}
	}

	@Override
	public LockResponse acquireLock(LockInfo lockInfo,
			Class<? extends ILockServiceContributor> lockServiceContributorClass) {
		if (lockInfo == null) {
			return LockResponse.DENIED(lockInfo);
		}

		// TODO what if user and system id are ident?
		LockInfo lie = locks.get(lockInfo.getElementId());
		if (lie != null) {
			if (lie.getUser().equals(lockInfo.getUser()) && lie.getSystemUuid().equals(lockInfo.getSystemUuid())) {
				// its the requesters lock (username and systemUuid match)
				return LockResponse.OK;
			} else {
				return LockResponse.DENIED(lie);
			}
		}

		synchronized (locks) {
			// is there an entry for any requested element
			synchronized (contributors) {
				if (contributors.size() == 0 && failOnMissingLockServiceContributor) {
					log.error("System defined to require a lock service contributor. None available, denying locks!");
					return new LockResponse(Status.ERROR, null);
				}

				for (ILockServiceContributor iLockServiceContributor : contributors) {
					if (iLockServiceContributor.getClass().equals(lockServiceContributorClass)) {
						continue;
					}

					LockResponse lr = iLockServiceContributor.acquireLock(lockInfo);
					if (!lr.isOk()) {
						return lr;
					}
				}
			}

			locks.put(lockInfo.getElementId(), lockInfo);

			return LockResponse.OK;
		}
	}

	@Override
	public LockResponse acquireLock(final LockInfo lockInfo) {
		return acquireLock(lockInfo, null);
	}

	@Override
	public LockResponse releaseLock(LockInfo lockInfo,
			Class<? extends ILockServiceContributor> lockServiceContributorClass) {
		if (lockInfo == null) {
			return LockResponse.DENIED(lockInfo);
		}

		synchronized (locks) {
			synchronized (contributors) {
				for (ILockServiceContributor iLockServiceContributor : contributors) {
					if (iLockServiceContributor.getClass().equals(lockServiceContributorClass)) {
						continue;
					}

					LockResponse lr = iLockServiceContributor.releaseLock(lockInfo);
					if (!lr.isOk()) {
						return lr;
					}
				}
			}

			locks.remove(lockInfo.getElementId());

			return LockResponse.OK;
		}
	}

	@Override
	public LockResponse releaseLock(final LockInfo lockInfo) {
		return releaseLock(lockInfo, null);
	}

	@Override
	public boolean isLocked(String storeToString) {
		String elementId = LockInfo.getElementId(storeToString);
		return locks.get(elementId) != null;
	}

	@Override
	public Optional<LockInfo> getLockInfo(String storeToString) {
		String elementId = LockInfo.getElementId(storeToString);
		return Optional.ofNullable(locks.get(elementId));
	}

	public static List<LockInfo> getAllLockInfo() {
		return new ArrayList<LockInfo>(locks.values());
	}

	public static void clearAllLocks() {
		locks.clear();
	}

	public static String consoleListLocks() {
		StringBuilder sb = new StringBuilder();
		for (LockInfo lockInfo : getAllLockInfo()) {
			sb.append(lockInfo.getUser() + "@" + lockInfo.getElementType() + "::" + lockInfo.getElementId() + "\t"
					+ lockInfo.getCreationDate() + "\t[" + lockInfo.getSystemUuid() + "]\n");
		}
		return sb.toString();
	}

	public static String getSystemuuid() {
		return systemUuid.toString();
	}

}
