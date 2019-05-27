package info.elexis.server.core.connector.elexis.services;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockRequest;
import ch.elexis.core.lock.types.LockRequest.Type;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.server.ILockService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.ILocalLockService;
import ch.elexis.core.services.IStoreToStringService;

@Component
public class LocalLockService implements ILocalLockService {
	
	@Reference
	private ILockService lockService;
	
	@Reference
	private IStoreToStringService storeToStringService;
	
	@Reference
	private IContextService contextService;
	
	@Override
	public LockResponse acquireOrReleaseLocks(LockRequest request){
		return lockService.acquireOrReleaseLocks(request);
	}
	
	@Override
	public boolean isLocked(LockRequest request){
		return lockService.isLocked(request);
	}
	
	@Override
	public LockInfo getLockInfo(String storeToString){
		return lockService.getLockInfo(storeToString);
	}
	
	private LockRequest buildLockRequest(Identifiable object, Type lockType){
		String storeToString = storeToStringService.storeToString(object).orElse(null);
		String userId = contextService.getActiveUser().map(IUser::getId).orElse("unknownUser");
		LockInfo lockInfo =
			new LockInfo(storeToString, userId, contextService.getStationIdentifier());
		return new LockRequest(lockType, lockInfo);
	}
	
	@Override
	public LockResponse acquireLock(Object object){
		if (object instanceof Identifiable) {
			LockRequest lockRequest =
				buildLockRequest((Identifiable) object, LockRequest.Type.ACQUIRE);
			return acquireOrReleaseLocks(lockRequest);
		}
		throw new IllegalArgumentException("Can not acquireLock on class " + object);
	}
	
	@Override
	public LockResponse releaseLock(Object object){
		if (object instanceof Identifiable) {
			LockRequest lockRequest =
				buildLockRequest((Identifiable) object, LockRequest.Type.RELEASE);
			return acquireOrReleaseLocks(lockRequest);
		}
		throw new IllegalArgumentException("Can not releaseLock on class " + object);
	}
	
	@Override
	public LockResponse releaseLock(LockInfo lockInfo){
		if (lockInfo.getElementStoreToString() == null) {
			return LockResponse.DENIED(null);
		}
		return releaseLock(lockInfo.getElementStoreToString());
	}
	
	@Override
	public boolean isLocked(Object object){
		if (object instanceof Identifiable) {
			LockRequest request = buildLockRequest((Identifiable) object, LockRequest.Type.INFO);
			return lockService.isLocked(request);
		}
		throw new IllegalArgumentException("Can not isLocked on class " + object);
	}
	
	@Override
	public boolean isLockedLocal(Object object){
		return isLocked(object);
	}
	
	@Override
	public LockResponse releaseAllLocks(){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public List<LockInfo> getCopyOfAllHeldLocks(){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getSystemUuid(){
		return contextService.getStationIdentifier();
	}
	
	@Override
	public LockResponse acquireLockBlocking(Object po, int msTimeout, IProgressMonitor monitor){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Status getStatus(){
		return Status.LOCAL;
	}
	
	@Override
	public void shutdown(){}
	
}
