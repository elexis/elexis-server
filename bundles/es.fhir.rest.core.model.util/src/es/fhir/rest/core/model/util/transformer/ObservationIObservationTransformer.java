package es.fhir.rest.core.model.util.transformer;

import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ca.uhn.fhir.model.api.Include;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.IObservation;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.model.util.transformer.helper.FindingsContentHelper;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.KontaktService;

@Component
public class ObservationIObservationTransformer
		implements IFhirTransformer<Observation, IObservation> {

	private FindingsContentHelper contentHelper = new FindingsContentHelper();

	private IFindingsService findingsService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFindingsService(IFindingsService findingsService) {
		this.findingsService = findingsService;
	}

	@Override
	public Optional<Observation> getFhirObject(IObservation localObject, Set<Include> includes) {
		Optional<IBaseResource> resource = contentHelper.getResource(localObject);
		if (resource.isPresent()) {
			return Optional.of((Observation) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public Optional<IObservation> getLocalObject(Observation fhirObject) {
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<IObservation> existing =
				findingsService.findById(fhirObject.getId(), IObservation.class);
			if (existing.isPresent()) {
				return Optional.of(existing.get());
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<IObservation> updateLocalObject(Observation fhirObject, IObservation localObject) {
		return Optional.empty();
	}

	@Override
	public Optional<IObservation> createLocalObject(Observation fhirObject) {
		IObservation iObservation = findingsService.create(IObservation.class);
		contentHelper.setResource(fhirObject, iObservation);
		if (fhirObject.getSubject() != null && fhirObject.getSubject().hasReference()) {
			String id = fhirObject.getSubject().getReferenceElement().getIdPart();
			Optional<Kontakt> patient = KontaktService.load(id);
			patient.ifPresent(k -> iObservation.setPatientId(id));
		}
		IEncounter iEncounter = null;
		if (fhirObject.getContext() != null && fhirObject.getContext().hasReference()) {
			String id = fhirObject.getContext().getReferenceElement().getIdPart();
			Optional<IEncounter> encounter = findingsService.findById(id, IEncounter.class);
			if (encounter.isPresent()) {
				iEncounter = encounter.get();
				iObservation.setEncounter(iEncounter);
			}
		}
		findingsService.saveFinding(iObservation);
		return Optional.of(iObservation);
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Observation.class.equals(fhirClazz) && IObservation.class.equals(localClazz);
	}

}
