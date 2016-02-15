package info.elexis.server.core.connector.elexis.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.connector.elexis.locking.ILockService;
import info.elexis.server.core.connector.elexis.locking.ILockServiceContributor;
import info.elexis.server.elexis.common.types.LockInfo;

@Component(service = {})
public class LockService implements ILockService {

	private static HashMap<String, LockInfo> locks = new HashMap<String, LockInfo>();
	private static List<ILockServiceContributor> contributors = new ArrayList<ILockServiceContributor>();

	private static Logger log = LoggerFactory.getLogger(LockService.class);

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
	public boolean acquireLocks(final List<LockInfo> lockInfos) {
		if (lockInfos == null) {
			return false;
		}

		for (LockInfo li : lockInfos) {
			// TODO what if user and system id are ident?
			LockInfo lie = locks.get(li.getElementId());
			if (lie != null) {
				if (lie.getUser().equals(li.getUser()) && lie.getSystemUuid().equals(li.getSystemUuid())) {
					// its the requesters lock (username and systemUuid match)
					return true;
				} else {
					return false;
				}
			}
		}

		synchronized (locks) {
			// is there an entry for any requested element
			synchronized (contributors) {
				for (ILockServiceContributor iLockServiceContributor : contributors) {
					if (!iLockServiceContributor.acquireLocks(lockInfos)) {
						return false;
					}
				}
			}

			lockInfos.stream().forEach(o -> locks.put(o.getElementId(), o));

			return true;
		}
	}

	@Override
	public boolean releaseLocks(final List<LockInfo> lockInfos) {
		if (lockInfos == null) {
			return false;
		}

		synchronized (locks) {
			synchronized (contributors) {
				for (ILockServiceContributor iLockServiceContributor : contributors) {
					if (!iLockServiceContributor.releaseLocks(lockInfos)) {
						return false;
					}
				}
			}

			lockInfos.stream().forEach(o -> locks.remove(o.getElementId()));

			return true;
		}
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
					+ lockInfo.getCreationDate() + "\t[" + lockInfo.getSystemUuid().toString() + "]\n");
		}
		return sb.toString();
	}

}
