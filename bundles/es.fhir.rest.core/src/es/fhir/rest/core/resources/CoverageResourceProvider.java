package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.IdType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IModelService;

@Component(service = IFhirResourceProvider.class)
public class CoverageResourceProvider
		extends AbstractFhirCrudResourceProvider<Coverage, ICoverage> {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;
	
	@Reference
	private IFhirTransformerRegistry transformerRegistry;
	
	public CoverageResourceProvider(){
		super(ICoverage.class);
	}
	
	@Override
	public Class<? extends IBaseResource> getResourceType(){
		return Coverage.class;
	}
	
	@Activate
	public void activate(){
		super.setCoreModelService(coreModelService);
	}
	
	@Override
	public IFhirTransformer<Coverage, ICoverage> getTransformer(){
		return (IFhirTransformer<Coverage, ICoverage>) transformerRegistry
			.getTransformerFor(Coverage.class, ICoverage.class);
	}
	
	@Search()
	public List<Coverage> findCoverageByBeneficiary(
		@RequiredParam(name = Coverage.SP_BENEFICIARY) IdType theBeneficiaryId){
		if (theBeneficiaryId != null) {
			Optional<IPatient> patient =
				coreModelService.load(theBeneficiaryId.getIdPart(), IPatient.class);
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
	
}
