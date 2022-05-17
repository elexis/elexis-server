package es.fhir.rest.core.resources;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Slot;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.services.IModelService;

@Component(service = IFhirResourceProvider.class)
public class SlotResourceProvider extends AbstractFhirCrudResourceProvider<Slot, IAppointment> {
	
	public SlotResourceProvider(){
		super(IAppointment.class);
	}
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;
	
	@Reference
	private IFhirTransformerRegistry transformerRegistry;
	
	@Override
	public Class<? extends IBaseResource> getResourceType(){
		return Slot.class;
	}
	
	@Activate
	public void activate(){
		super.setCoreModelService(coreModelService);
	}
	
	@Override
	public IFhirTransformer<Slot, IAppointment> getTransformer(){
		return (IFhirTransformer<Slot, IAppointment>) transformerRegistry
			.getTransformerFor(Slot.class, IAppointment.class);
	}
	
}
