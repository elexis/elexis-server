package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Coverage;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IModelService;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;

@Component
public class CoverageResourceProvider implements IFhirResourceProvider {
	
	private Logger log;
	private ResourceProviderUtil resourceProviderUtil;
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;
	
	@Reference
	private IFhirTransformerRegistry transformerRegistry;
	
	@Override
	public Class<? extends IBaseResource> getResourceType(){
		return Coverage.class;
	}
	
	@Activate
	public void activate() {
		log = LoggerFactory.getLogger(getClass());
		resourceProviderUtil = new ResourceProviderUtil();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Coverage, ICoverage> getTransformer(){
		return (IFhirTransformer<Coverage, ICoverage>) transformerRegistry
			.getTransformerFor(Coverage.class, ICoverage.class);
	}
	
	@Read
	public Coverage getResourceById(@IdParam IdType theId){
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<ICoverage> coverage = modelService.load(idPart, ICoverage.class);
			if (coverage.isPresent()) {
				Optional<Coverage> fhirCoverage = getTransformer().getFhirObject(coverage.get());
				return fhirCoverage.get();
			}
		}
		return null;
	}
	
	@Search()
	public List<Coverage> findCoverageByBeneficiary(
		@RequiredParam(name = Coverage.SP_BENEFICIARY) IdType theBeneficiaryId){
		if (theBeneficiaryId != null) {
			Optional<IPatient> patient =
				modelService.load(theBeneficiaryId.getIdPart(), IPatient.class);
			if (patient.isPresent()) {
				List<ICoverage> faelle = patient.get().getCoverages();
				if (faelle != null) {
					List<Coverage> ret = new ArrayList<Coverage>();
					for (ICoverage fall : faelle) {
						Optional<Coverage> fhirCoverage = getTransformer().getFhirObject(fall);
						fhirCoverage.ifPresent(fp -> ret.add(fp));
					}
					return ret;
				}
			}
		}
		return Collections.emptyList();
	}
	
	@Create
	public MethodOutcome createCoverage(@ResourceParam Coverage coverage){
		return resourceProviderUtil.createResource(getTransformer(), coverage, log);
	}
}
