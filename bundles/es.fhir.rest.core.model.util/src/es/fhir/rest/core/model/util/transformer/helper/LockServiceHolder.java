package es.fhir.rest.core.model.util.transformer.helper;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import info.elexis.server.core.connector.elexis.locking.ILockService;

@Component
public class LockServiceHolder {
	
	private static ILockService lockService;
	
	@Reference
	public void setLockService(ILockService lockService){
		LockServiceHolder.lockService = lockService;
	}
	
	public static ILockService get(){
		if (lockService == null) {
			throw new IllegalStateException("No " + ILockService.class.getName() + " available");
		}
		return lockService;
	}
}