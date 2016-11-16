package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Coverage;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.FallService;
import info.elexis.server.core.connector.elexis.services.KontaktService;

@Component
public class CoverageResourceProvider implements IFhirResourceProvider {

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Coverage.class;
	}

	private IFhirTransformerRegistry transformerRegistry;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFhirTransformerRegistry(IFhirTransformerRegistry transformerRegistry) {
		this.transformerRegistry = transformerRegistry;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Coverage, Fall> getTransformer() {
		return (IFhirTransformer<Coverage, Fall>) transformerRegistry.getTransformerFor(Coverage.class,
					Fall.class);
	}

	@Read
	public Coverage getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<Fall> coverage = FallService.INSTANCE.findById(idPart);
			if (coverage.isPresent()) {
				Optional<Coverage> fhirCoverage = getTransformer().getFhirObject(coverage.get());
				return fhirCoverage.get();
			}
		}
		return null;
	}

	@Search()
	public List<Coverage> findCoverageByBeneficiary(
			@RequiredParam(name = Coverage.SP_BENEFICIARY_REFERENCE) IdType theBeneficiaryId) {
		if (theBeneficiaryId != null) {
			Optional<Kontakt> patient = KontaktService.INSTANCE.findById(theBeneficiaryId.getIdPart());
			if (patient.isPresent()) {
				List<Fall> faelle = KontaktService.getFaelle(patient.get());
				if (faelle != null) {
					List<Coverage> ret = new ArrayList<Coverage>();
					for (Fall fall : faelle) {
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
