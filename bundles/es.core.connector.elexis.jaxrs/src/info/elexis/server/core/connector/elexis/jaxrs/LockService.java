package info.elexis.server.core.connector.elexis.jaxrs;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockRequest;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.lock.types.LockResponse.Status;
import ch.elexis.core.server.ILockService;
import info.elexis.server.core.connector.elexis.locking.LockServiceInstance;

@Component(service = LockService.class, immediate = true)
public class LockService implements ILockService {
	
	@Override
	public boolean isLocked(String objectId) {
		return LockServiceInstance.INSTANCE.isLocked(objectId);
	}

	@Override
	public LockInfo getLockInfo(String objectId) {
		Optional<LockInfo> li = LockServiceInstance.INSTANCE.getLockInfo(objectId);
		return (li.isPresent()) ? li.get() : null;
	}

	@Override
	public LockResponse acquireOrReleaseLocks(LockRequest request) {
		switch (request.getRequestType()) {
		case ACQUIRE:
			return LockServiceInstance.INSTANCE.acquireLock(request.getLockInfo());
		case RELEASE:
			return LockServiceInstance.INSTANCE.releaseLock(request.getLockInfo());
		}
		return new LockResponse(Status.ERROR, null);
	}
}
