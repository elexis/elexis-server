package es.fhir.rest.core.resources;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;

@Component
public class ConditionResourceProvider implements IFhirResourceProvider {

	private IFindingsService findingsService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFindingsService(IFindingsService findingsService) {
		this.findingsService = findingsService;
	}

	private IFhirTransformerRegistry transformerRegistry;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFhirTransformerRegistry(IFhirTransformerRegistry transformerRegistry) {
		this.transformerRegistry = transformerRegistry;
	}

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Condition.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Condition, ICondition> getTransformer() {
		return (IFhirTransformer<Condition, ICondition>) transformerRegistry
					.getTransformerFor(Condition.class, ICondition.class);
	}

	@Read
	public Condition getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<IFinding> condition = findingsService.findById(idPart);
			if (condition.isPresent() && (condition.get() instanceof ICondition)) {
				Optional<Condition> fhirEncounter = getTransformer().getFhirObject((ICondition) condition.get());
				return fhirEncounter.get();
			}
		}
		return null;
	}
}
