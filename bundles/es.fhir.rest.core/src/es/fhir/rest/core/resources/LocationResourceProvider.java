package es.fhir.rest.core.resources;

import java.util.Objects;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;

@Component
public class LocationResourceProvider implements IFhirResourceProvider {
	
	@Reference
	private IFhirTransformerRegistry transformerRegistry;
	
	@Override
	public Class<? extends IBaseResource> getResourceType(){
		return Location.class;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Location, String> getTransformer(){
		return (IFhirTransformer<Location, String>) transformerRegistry
			.getTransformerFor(Location.class, String.class);
	}
	
	@Read
	public Location getResourceById(@IdParam
	IdType theId){
		String idPart = theId.getIdPart();
		if (Objects.equals("mainLocation", idPart)) {
			return getTransformer().getFhirObject(idPart).get();
		}
		return null;
	}
	
}
