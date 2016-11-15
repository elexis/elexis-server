package info.elexis.server.core.connector.elexis.services;

import java.util.ArrayList;
import java.util.Collections;
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
import info.elexis.server.core.connector.elexis.Properties;
import info.elexis.server.core.connector.elexis.locking.ILockService;
import info.elexis.server.core.connector.elexis.locking.ILockServiceContributor;

@Component(service = {})
public class LockService implements ILockService {

	private static HashMap<String, LockInfo> locks = new HashMap<String, LockInfo>();
	private static ReentrantLock locksLock = new ReentrantLock();
	private static Map<String, ILockServiceContributor> contributors = new HashMap<String, ILockServiceContributor>();
	private static Set<String> requiredContributors;

	private static Logger log = LoggerFactory.getLogger(LockService.class);

	static {
		Boolean acceptMissingContributors = Boolean
				.valueOf(System.getProperty(Properties.SYSTEM_PROPERTY_ACCEPT_MISSING_LOCKSERVICE_CONTRIBUTORS));
		if (acceptMissingContributors) {
			log.warn("!!! WILL ACCEPT MISSING LOCK SERVICE CONTRIBUTORS !!!");
			requiredContributors = Collections.emptySet();
		} else {
			requiredContributors = LocalProperties
					.getPropertyAsSet(Properties.PROPERTY_CONFIG_REQUIRED_LOCK_CONTRIBUTORS);
		}
	}

	/**
	 * A unique id for this instance of Elexis. Changes on every restart
	 */
	public static final UUID systemUuid = UUID.randomUUID();
	
	/**
	 * The elexis-server itself acts on a lock
	 */
	public static final String elexisServerAgentUser = "__elexis-server__";

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

				log.warn("Denying lock release for [{}]",
						lockInfo.getElementStoreToString() + "#" + lockInfo.getUser() + "@" + lockInfo.getSystemUuid());
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

	/**
	 * Removes all locks without performing the respective releaseLock
	 * operations
	 */
	public static void clearAllLocks() {
		locks.clear();
	}

	/**
	 * Removes a lock without performing the respective releaseLock operation
	 * 
	 * @param elementId
	 * @return <code>true</code> on success else <code>false</code>
	 */
	public static boolean clearLock(String elementId) {
		if (locksLock.tryLock()) {
			try {
				if (locks.containsKey(elementId)) {
					locks.remove(elementId);
					return true;
				}
			} finally {
				if (locksLock.isHeldByCurrentThread()) {
					locksLock.unlock();
				}
			}
		}
		return false;
	}

	public static String getSystemuuid() {
		return systemUuid.toString();
	}
	
	public static String getElexisserveragentuser() {
		return elexisServerAgentUser;
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
