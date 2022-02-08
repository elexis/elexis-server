package es.fhir.rest.core.resources;

import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Invoice;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.services.IModelService;

@Component
public class InvoiceResourceProvider implements IFhirResourceProvider<Invoice, IInvoice> {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;
	
	@Reference
	private IFhirTransformerRegistry transformerRegistry;
	
	@Override
	public Class<? extends IBaseResource> getResourceType(){
		return Invoice.class;
	}
	
	@Override
	public IFhirTransformer<Invoice, IInvoice> getTransformer(){
		return (IFhirTransformer<Invoice, IInvoice>) transformerRegistry
			.getTransformerFor(Invoice.class, IInvoice.class);
	}
	
	@Search(queryName = "by-encounter")
	public Invoice searchByEncounter(
		@RequiredParam(name = "encounter") ReferenceParam encounterParam, @IncludeParam(allow = {
			"Invoice.lineItem.chargeItem"
		}) Set<Include> theIncludes){
			
		IFhirTransformer<Invoice, IEncounter> encounterInvoiceTransformer =
			(IFhirTransformer<Invoice, IEncounter>) transformerRegistry
				.getTransformerFor(Invoice.class, IEncounter.class);
		
		String encounterId = encounterParam.getIdPart();
		
		Optional<IEncounter> encounter = coreModelService.load(encounterId, IEncounter.class);
		if (encounter.isPresent()) {
			Optional<Invoice> fhirObject =
				encounterInvoiceTransformer.getFhirObject(encounter.get(), null, theIncludes);
			if (fhirObject.isPresent()) {
				return fhirObject.get();
			}
		}
		
		return null;
	}
	
}
