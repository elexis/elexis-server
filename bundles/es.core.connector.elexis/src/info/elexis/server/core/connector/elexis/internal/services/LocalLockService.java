package info.elexis.server.core.connector.elexis.internal.services;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockRequest;
import ch.elexis.core.lock.types.LockRequest.Type;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.ILocalLockService;
import ch.elexis.core.services.IStoreToStringService;
import info.elexis.server.core.SystemPropertyConstants;
import info.elexis.server.core.connector.elexis.locking.ILockService;

@Component
public class LocalLockService implements ILocalLockService {
	
	/**
	 * The elexis-server itself acts on a lock
	 */
	public static final String ELEXISSERVER_AGENTUSER = "__elexis-server__";
	
	private Logger log;
	
	@Reference
	private ILockService lockService;
	
	@Reference
	private IStoreToStringService storeToStringService;
	
	@Reference
	private IContextService contextService;
	
	@Activate
	public void activate(){
		log = LoggerFactory.getLogger(getClass());
	}
	
	@Override
	public LockResponse acquireOrReleaseLocks(LockRequest request){
		if (Type.ACQUIRE == request.getRequestType()) {
			return lockService.acquireLock(request.getLockInfo());
		} else if (Type.RELEASE == request.getRequestType()) {
			return lockService.releaseLock(request.getLockInfo());
		} else if (Type.INFO == request.getRequestType()) {
			// what??
		}
		throw new IllegalArgumentException(
			"No support for request type " + request.getRequestType());
	}
	
	@Override
	public boolean isLocked(LockRequest request){
		return lockService.isLocked(request.getLockInfo());
	}
	
	@Override
	public LockInfo getLockInfo(String storeToString){
		return lockService.getLockInfo(storeToString).orElse(null);
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
			return lockService.isLocked(request.getLockInfo());
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
	public LockResponse acquireLockBlocking(Object po, int timeout, IProgressMonitor monitor){
		if (po instanceof Identifiable) {
			Identifiable lobj = (Identifiable) po;
			String sts = storeToStringService.storeToString(lobj).orElse(null);
			LockInfo ls =
				new LockInfo(sts, ELEXISSERVER_AGENTUSER, SystemPropertyConstants.getStationId());
			log.trace("Trying to acquire lock blocking ({}sec) for [{}].", timeout, sts);
			LockResponse lr = lockService.acquireLockBlocking(ls, timeout);
			if (!lr.isOk()) {
				log.error("Failed acquiring lock for [{}]",
					lobj.getClass().getName() + "@" + lobj.getId(), new Throwable("Diagnosis"));
			}
			return lr;
		}
		return LockResponse.ERROR;
	}
	
	@Override
	public Status getStatus(){
		return Status.LOCAL;
	}
	
	@Override
	public void shutdown(){}
	
}
