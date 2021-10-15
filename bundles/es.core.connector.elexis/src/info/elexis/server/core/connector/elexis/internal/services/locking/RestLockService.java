package info.elexis.server.core.connector.elexis.internal.services.locking;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockRequest;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.lock.types.LockResponse.Status;
import ch.elexis.core.server.ILockService;

@Component
public class RestLockService implements ILockService {
	
	@Reference
	private info.elexis.server.core.connector.elexis.locking.ILockService theLockService;
	
	@Override
	public boolean isLocked(LockRequest request){
		return theLockService.isLocked(request.getLockInfo());
	}
	
	@Override
	public LockInfo getLockInfo(String objectId){
		if (objectId == null) {
			return null;
		}
		
		try {
			Optional<LockInfo> li = theLockService.getLockInfo(objectId);
			return (li.isPresent()) ? li.get() : null;
		} catch (IllegalArgumentException iae) {
			return null;
		}
	}
	
	@Override
	public LockResponse acquireOrReleaseLocks(LockRequest request){
		// System.out.println("******************** "+request.getRequestType()+"
		// "+request.getLockInfo().getElementStoreToString());
		LockResponse lr = new LockResponse(Status.ERROR, null);
		switch (request.getRequestType()) {
		case ACQUIRE:
			lr = theLockService.acquireLock(request.getLockInfo());
			break;
		case RELEASE:
			lr = theLockService.releaseLock(request.getLockInfo());
			break;
		default:
			break;
		}
		// System.out.println("******************** "+lr.getStatus()+"
		// "+request.getLockInfo().getElementStoreToString());
		return lr;
	}
}
