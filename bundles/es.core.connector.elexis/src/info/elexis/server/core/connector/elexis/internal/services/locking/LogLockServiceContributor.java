package info.elexis.server.core.connector.elexis.internal.services.locking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockResponse;
import info.elexis.server.core.connector.elexis.locking.ILockServiceContributor;

public class LogLockServiceContributor implements ILockServiceContributor {
	
	private Logger log;
	private Marker marker;
	
	public LogLockServiceContributor(){
		log = LoggerFactory.getLogger(getClass());
		marker = MarkerFactory.getMarker("LOCKING");
	}
	
	@Override
	public LockResponse acquireLock(LockInfo lockInfos){
		log.info(marker, "(ACQ) " + toLogString(lockInfos));
		return LockResponse.OK(lockInfos);
	}
	
	@Override
	public LockResponse releaseLock(LockInfo lockInfos){
		log.info(marker, "(REL) " + toLogString(lockInfos));
		return LockResponse.OK(lockInfos);
	}
	
	private String toLogString(LockInfo lockInfos){
		if (lockInfos == null) {
			return "null";
		}
		return lockInfos.getCreationDate() + ": " + lockInfos.getUser() + "@"
			+ lockInfos.getSystemUuid() + " (" + lockInfos.getStationId() + "/"
			+ lockInfos.getStationLabel() + ") " + lockInfos.getElementType() + "|"
			+ lockInfos.getElementId() + "|" + lockInfos.getElementStoreToString();
	}
	
}
