package es.fhir.rest.core.resources;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Slot;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.services.IModelService;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;

@Component
public class SlotResourceProvider implements IFhirResourceProvider {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;
	
	@Reference
	private IFhirTransformerRegistry transformerRegistry;
	
	@Override
	public Class<? extends IBaseResource> getResourceType(){
		return Slot.class;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Slot, IAppointment> getTransformer(){
		return (IFhirTransformer<Slot, IAppointment>) transformerRegistry
			.getTransformerFor(Slot.class, IAppointment.class);
	}
	
	@Read
	public Slot getResourceById(@IdParam
	IdType theId){
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<IAppointment> appointment = modelService.load(idPart, IAppointment.class);
			if (appointment.isPresent()) {
				Optional<Slot> fhirAppointment = getTransformer().getFhirObject(appointment.get());
				return fhirAppointment.get();
			}
		}
		return null;
	}
	
}
