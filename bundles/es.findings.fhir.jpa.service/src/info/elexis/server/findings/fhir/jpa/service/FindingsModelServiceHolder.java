package info.elexis.server.findings.fhir.jpa.service;

import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IModelService;

public class FindingsModelServiceHolder {
	private static IModelService modelService;

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.findings.model)")
	public void setModelService(IModelService modelService) {
		FindingsModelServiceHolder.modelService = modelService;
	}

	public static IModelService get() {
		if (modelService == null) {
			throw new IllegalStateException("No IModelService available");
		}
		return modelService;
	}
}
