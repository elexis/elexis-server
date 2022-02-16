package es.fhir.rest.core.resources;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ChargeItem;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.IBilled;

@Component(service = IFhirResourceProvider.class)
public class ChargeItemResourceProvider
		extends AbstractFhirCrudResourceProvider<ChargeItem, IBilled> {
	
	@Reference
	private IFhirTransformerRegistry transformerRegistry;
	
	public ChargeItemResourceProvider(){
		super(IBilled.class);
	}
	
	@Override
	public Class<? extends IBaseResource> getResourceType(){
		return ChargeItem.class;
	}
	
	@Override
	public IFhirTransformer<ChargeItem, IBilled> getTransformer(){
		return (IFhirTransformer<ChargeItem, IBilled>) transformerRegistry
			.getTransformerFor(ChargeItem.class, IBilled.class);
	}
	
}
