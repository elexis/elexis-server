package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
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
	private static ReentrantLock locksLock = new ReentrantLock();
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
	public LockResponse acquireLock(final LockInfo lockInfo) {
		return acquireLock(lockInfo, null);
	}

	@Override
	public LockResponse acquireLockBlocking(LockInfo lockInfo, int timeout) {
		return acquireLockBlocking(lockInfo, null, timeout);
	}

	@Override
	public LockResponse acquireLock(LockInfo lockInfo,
			Class<? extends ILockServiceContributor> lockServiceContributorClass) {
		if (lockInfo == null) {
			return LockResponse.DENIED(lockInfo);
		}

		// TODO what if user and system id are ident?
		try {
			if (locksLock.tryLock()) {
				LockInfo lie = locks.get(lockInfo.getElementId());
				if (lie != null) {
					if (lie.getUser().equals(lockInfo.getUser())
							&& lie.getSystemUuid().equals(lockInfo.getSystemUuid())) {
						// its the requesters lock (username and systemUuid
						// match)
						return LockResponse.OK;
					} else {
						return LockResponse.DENIED(lie);
					}
				}

				// is there an entry for any requested element
				synchronized (contributors) {
					if (!contributors.keySet().containsAll(requiredContributors)) {
						log.warn(
								"System defined to require a lock service contributor. None available, denying locks!");
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
			} else {
				log.warn("Could not acquire locksLock in acquireLock method on thread {}. ", Thread.currentThread());
				return LockResponse.DENIED(lockInfo);
			}
		} finally {
			if (locksLock.isHeldByCurrentThread()) {
				locksLock.unlock();
			}
		}
	}

	@Override
	public LockResponse acquireLockBlocking(LockInfo lockInfo,
			Class<? extends ILockServiceContributor> lockServiceContributorClass, int timeout) {
		LockResponse response = (lockServiceContributorClass != null)
				? acquireLock(lockInfo, lockServiceContributorClass) : acquireLock(lockInfo);
		int sleptMilli = 0;
		while (!response.isOk()) {
			try {
				Thread.sleep(1000);
				sleptMilli += 1000;
				log.trace("Retry acquire lock blocking ({} sec) for [{}].", Integer.toString(sleptMilli),
						lockInfo.getElementStoreToString());
				response = (lockServiceContributorClass != null) ? acquireLock(lockInfo, lockServiceContributorClass)
						: acquireLock(lockInfo);
				if (response.getStatus() == LockResponse.Status.DENIED_PERMANENT) {
					return response;
				}
				if (sleptMilli > (timeout * 1000)) {
					log.warn("Timeout acquiring lock blocking ({} sec) for [{}].", Integer.toString(timeout),
							lockInfo.getElementStoreToString());
					return response;
				}
			} catch (InterruptedException e) {
				// ignore and keep trying
			}
		}
		return response;
	}

	@Override
	public LockResponse releaseLock(LockInfo lockInfo,
			Class<? extends ILockServiceContributor> lockServiceContributorClass) {
		if (lockInfo == null) {
			return LockResponse.DENIED(lockInfo);
		}

		try {
			if (locksLock.tryLock()) {
				LockInfo lie = locks.get(lockInfo.getElementId());
				if (lie == null) {
					log.warn("Releasing lock for object not in locks map [{}]", lockInfo.getElementStoreToString() + "#"
							+ lockInfo.getUser() + "@" + lockInfo.getSystemUuid());
				}

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

				if (lie != null) {
					if (lie.getUser().equals(lockInfo.getUser())
							&& lie.getSystemUuid().equals(lockInfo.getSystemUuid())) {
						locks.remove(lockInfo.getElementId());
						return LockResponse.OK;
					}
				}

				return LockResponse.DENIED(lockInfo);
			} else {
				log.warn("Could not acquire locksLock in releaseLock method on thread {}. ", Thread.currentThread());
				return LockResponse.DENIED(lockInfo);
			}
		} finally {
			if (locksLock.isHeldByCurrentThread()) {
				locksLock.unlock();
			}
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
		sb.append("======= " + LocalDateTime.now() + " ==== server uuid [" + LockService.getSystemuuid() + "]\n");
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
			try {
				List<LockInfo> eviction = new ArrayList<>();
				// collect LockInfos ready for eviction

				try {
					if (locksLock.tryLock(2, TimeUnit.SECONDS)) {
						long currentMillis = System.currentTimeMillis();
						Set<String> keys = locks.keySet();
						for (String key : keys) {
							LockInfo lockInfo = locks.get(key);
							// do not evict locks set by server system
							if (getSystemuuid().equals(lockInfo.getSystemUuid())) {
								continue;
							}
							if (lockInfo.evict(currentMillis)) {
								eviction.add(lockInfo);
							}
						}
					} else {
						log.warn("Could not acquire locksLock in LockEvictionTask#run method on thread {}. ",
								Thread.currentThread());
					}
				} catch (InterruptedException ie) {
					log.warn("Interrupted @ acquire locksLock in LockEvictionTask#run method on thread {}. ",
							Thread.currentThread(), ie);
				} finally {
					if (locksLock.isHeldByCurrentThread()) {
						locksLock.unlock();
					}
				}

				// release the collected locks
				for (LockInfo lockInfo : eviction) {
					logger.debug("Eviction releasing lock [" + lockInfo.getUser() + "@" + lockInfo.getElementType()
							+ StringConstants.DOUBLECOLON + lockInfo.getElementId() + "@" + lockInfo.getSystemUuid()
							+ "]");
					releaseLock(lockInfo);
				}
			} catch (Exception e) {
				logger.error("Error evicting lock", e);
			}
		}
	}
}
