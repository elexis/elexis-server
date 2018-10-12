package info.elexis.server.core.connector.elexis.services.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IModelService;

@Component
public class CoreModelServiceHolder {
	
	private static IModelService modelService;
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	public void setModelService(IModelService modelService){
		CoreModelServiceHolder.modelService = modelService;
	}
	
	public static IModelService get(){
		return modelService;
	}
}