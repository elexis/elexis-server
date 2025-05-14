package es.fhir.rest.core.resources;

import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CareTeam;
import org.hl7.fhir.r4.model.IdType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.services.IModelService;

@Component
public class CareTeamResourceProvider implements IFhirResourceProvider<CareTeam, IUserGroup> {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;
	
	@Reference
	private IFhirTransformerRegistry transformerRegistry;
	
	@Override
	public Class<? extends IBaseResource> getResourceType(){
		return CareTeam.class;
	}
	
	@Override
	public IFhirTransformer<CareTeam, IUserGroup> getTransformer() {
		return transformerRegistry
				.getTransformerFor(CareTeam.class, IUserGroup.class);
	}
	
	@Read
	public CareTeam getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<IUserGroup> group = modelService.load(idPart, IUserGroup.class);
			if (group.isPresent()) {
				Optional<CareTeam> fhirCareTeam = getTransformer().getFhirObject(group.get());
				return fhirCareTeam.get();
			}
		}
		return null;
	}
}
