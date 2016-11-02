package info.elexis.server.core.connector.elexis.jaxrs;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockRequest;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.lock.types.LockResponse.Status;
import ch.elexis.core.server.ILockService;
import info.elexis.server.core.connector.elexis.locking.LockServiceInstance;

@Component(service = LockService.class, immediate = true)
public class LockService implements ILockService {

	private Logger log = LoggerFactory.getLogger(LockService.class);

	@Override
	public boolean isLocked(LockRequest request) {
		return LockServiceInstance.INSTANCE.isLocked(request.getLockInfo());
	}

	@Override
	public LockInfo getLockInfo(String objectId) {
		Optional<LockInfo> li = LockServiceInstance.INSTANCE.getLockInfo(objectId);
		return (li.isPresent()) ? li.get() : null;
	}

	@Override
	public LockResponse acquireOrReleaseLocks(LockRequest request) {
		// System.out.println("******************** "+request.getRequestType()+"
		// "+request.getLockInfo().getElementStoreToString());
		LockResponse lr = new LockResponse(Status.ERROR, null);
		switch (request.getRequestType()) {
		case ACQUIRE:
			lr = LockServiceInstance.INSTANCE.acquireLock(request.getLockInfo());
			break;
		case RELEASE:
			lr = LockServiceInstance.INSTANCE.releaseLock(request.getLockInfo());
			break;
		default:
			break;
		}
		// System.out.println("******************** "+lr.getStatus()+"
		// "+request.getLockInfo().getElementStoreToString());
		return lr;
	}
}
