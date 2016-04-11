package info.elexis.server.core.connector.elexis.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
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
import info.elexis.server.core.common.LocalProperties;
import info.elexis.server.core.connector.elexis.internal.BundleConstants;
import info.elexis.server.core.connector.elexis.locking.ILockService;
import info.elexis.server.core.connector.elexis.locking.ILockServiceContributor;

@Component(service = {})
public class LockService implements ILockService {

	public static final String PROPERTY_CONFIG_REQUIRED_LOCK_CONTRIBUTORS = BundleConstants.BUNDLE_ID
			+ ".requiredLockContributors";

	private static HashMap<String, LockInfo> locks = new HashMap<String, LockInfo>();
	private static Map<String, ILockServiceContributor> contributors = new HashMap<String, ILockServiceContributor>();
	private static Set<String> requiredContributors = LocalProperties
			.getPropertyAsSet(PROPERTY_CONFIG_REQUIRED_LOCK_CONTRIBUTORS);

	private static Logger log = LoggerFactory.getLogger(LockService.class);

	/**
	 * A unique id for this instance of Elexis. Changes on every restart
	 */
	public static final UUID systemUuid = UUID.randomUUID();

	@Reference(service = ILockServiceContributor.class, cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "unsetLockServiceContributor")
	protected void setLockServiceContributor(ILockServiceContributor isc) {
		synchronized (contributors) {
			log.info("Binding lock service contributor " + isc.getClass());
			contributors.put(isc.getClass().getName(), isc);
		}
	}

	protected void unsetLockServiceContributor(ILockServiceContributor isc) {
		synchronized (contributors) {
			log.info("Unbinding lock service contributor " + isc.getClass());
			contributors.remove(isc.getClass().getName());
		}
	}

	private Timer timer;

	public LockService() {
		timer = new Timer();
		timer.schedule(new LockEvictionTask(), LockInfo.EVICTION_TIMEOUT, LockInfo.EVICTION_TIMEOUT);
	}

	@Override
	public LockResponse acquireLock(LockInfo lockInfo,
			Class<? extends ILockServiceContributor> lockServiceContributorClass) {
		if (lockInfo == null) {
			return LockResponse.DENIED(lockInfo);
		}

		// TODO what if user and system id are ident?

		synchronized (locks) {
			LockInfo lie = locks.get(lockInfo.getElementId());
			if (lie != null) {
				if (lie.getUser().equals(lockInfo.getUser()) && lie.getSystemUuid().equals(lockInfo.getSystemUuid())) {
					// its the requesters lock (username and systemUuid match)
					return LockResponse.OK;
				} else {
					return LockResponse.DENIED(lie);
				}
			}
			
			// is there an entry for any requested element
			synchronized (contributors) {
				if (!contributors.keySet().containsAll(requiredContributors)) {
					log.warn("System defined to require a lock service contributor. None available, denying locks!");
					return new LockResponse(Status.ERROR, null);
				}

				for (ILockServiceContributor iLockServiceContributor : contributors.values()) {
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
				for (ILockServiceContributor iLockServiceContributor : contributors.values()) {
					if (iLockServiceContributor.getClass().equals(lockServiceContributorClass)) {
						continue;
					}

					LockResponse lr = iLockServiceContributor.releaseLock(lockInfo);
					if (!lr.isOk()) {
						return lr;
					}
				}
			}

			LockInfo lie = locks.get(lockInfo.getElementId());
			if (lie != null) {
				if (lie.getUser().equals(lockInfo.getUser()) && lie.getSystemUuid().equals(lockInfo.getSystemUuid())) {
					locks.remove(lockInfo.getElementId());
					return LockResponse.OK;
				}
			}

			return LockResponse.DENIED(lockInfo);
		}
	}

	@Override
	public LockResponse releaseLock(final LockInfo lockInfo) {
		return releaseLock(lockInfo, null);
	}

	@Override
	public boolean isLocked(LockInfo lockInfo) {
		LockInfo lie = locks.get(lockInfo.getElementId());
		if (lie != null) {
			if (lie.getUser().equals(lockInfo.getUser()) && lie.getSystemUuid().equals(lockInfo.getSystemUuid())) {
				lie.refresh();
				return true;
			}
		}
		return false;
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

	private class LockEvictionTask extends TimerTask {

		private final Logger logger = LoggerFactory.getLogger(LockEvictionTask.class);

		@Override
		public void run() {
			List<LockInfo> eviction = new ArrayList<>();
			// collect LockInfos ready for eviction
			synchronized (locks) {
				long currentMillis = System.currentTimeMillis();
				Set<String> keys = locks.keySet();
				for (String key : keys) {
					LockInfo lockInfo = locks.get(key);
					// do not evict locks set by server system
					if (lockInfo.getSystemUuid().equals(LockService.systemUuid)) {
						continue;
					}
					if (lockInfo.evict(currentMillis)) {
						eviction.add(lockInfo);
					}
				}
			}
			// release the collected locks
			for (LockInfo lockInfo : eviction) {
				logger.debug("Eviction releasing lock [" + lockInfo.getUser() + "@" + lockInfo.getElementType() + "::"
						+ lockInfo.getElementId() + "@" + lockInfo.getSystemUuid() + "]");
				releaseLock(lockInfo);
			}
		}
	}
}
