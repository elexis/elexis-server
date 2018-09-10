package es.fhir.rest.core.model.util.transformer;

import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ca.uhn.fhir.model.api.Include;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IFindingsService;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.model.util.transformer.helper.FindingsContentHelper;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.KontaktService;

@Component(immediate = true)
public class ConditionIConditionTransformer implements IFhirTransformer<Condition, ICondition> {

	private FindingsContentHelper contentHelper = new FindingsContentHelper();

	private IFindingsService findingsService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFindingsService(IFindingsService findingsService) {
		this.findingsService = findingsService;
	}

	@Override
	public Optional<Condition> getFhirObject(ICondition localObject, Set<Include> includes) {
		Optional<IBaseResource> resource = contentHelper.getResource(localObject);
		if (resource.isPresent()) {
			return Optional.of((Condition) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public Optional<ICondition> getLocalObject(Condition fhirObject) {
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<ICondition> existing =
				findingsService.findById(fhirObject.getId(), ICondition.class);
			if (existing.isPresent()) {
				return Optional.of(existing.get());
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<ICondition> updateLocalObject(Condition fhirObject, ICondition localObject) {
		return Optional.empty();
	}

	@Override
	public Optional<ICondition> createLocalObject(Condition fhirObject) {
		ICondition iCondition = findingsService.create(ICondition.class);
		contentHelper.setResource(fhirObject, iCondition);
		if (fhirObject.getSubject() != null && fhirObject.getSubject().hasReference()) {
			String id = fhirObject.getSubject().getReferenceElement().getIdPart();
			Optional<Kontakt> patient = KontaktService.load(id);
			patient.ifPresent(k -> iCondition.setPatientId(id));
		}
		findingsService.saveFinding(iCondition);
		return Optional.of(iCondition);
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Condition.class.equals(fhirClazz) && ICondition.class.equals(localClazz);
	}

}
