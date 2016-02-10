package info.elexis.server.core.connector.elexis.jaxrs;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;

import info.elexis.server.core.connector.elexis.locking.LockServiceInstance;
import info.elexis.server.elexis.common.jaxrs.ILockService;
import info.elexis.server.elexis.common.types.LockInfo;
import info.elexis.server.elexis.common.types.LockRequest;

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
	public boolean acquireOrReleaseLocks(LockRequest request) {
		switch (request.getRequestType()) {
		case ACQUIRE:
			return LockServiceInstance.INSTANCE.acquireLocks(request.getLockInfos());
		case RELEASE:
			return LockServiceInstance.INSTANCE.releaseLocks(request.getLockInfos());
		}
		return false;
	}
}
