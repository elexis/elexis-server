package es.fhir.rest.core.model.util.transformer;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IFindingsService;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.transformer.helper.FindingsContentHelper;

@Component(immediate = true)
public class ConditionIConditionTransformer implements IFhirTransformer<Condition, ICondition> {

	private FindingsContentHelper contentHelper = new FindingsContentHelper();

	private IFindingsService findingsService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFindingsService(IFindingsService findingsService) {
		this.findingsService = findingsService;
	}

	@Override
	public Optional<Condition> getFhirObject(ICondition localObject) {
		Optional<IBaseResource> resource = contentHelper.getResource(localObject);
		if (resource.isPresent()) {
			return Optional.of((Condition) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public Optional<ICondition> getLocalObject(Condition fhirObject) {
		return Optional.empty();
	}

	@Override
	public Optional<ICondition> updateLocalObject(Condition fhirObject, ICondition localObject) {
		return Optional.empty();
	}

	@Override
	public Optional<ICondition> createLocalObject(Condition fhirObject) {
		return Optional.empty();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Condition.class.equals(fhirClazz) && ICondition.class.equals(localClazz);
	}

}
