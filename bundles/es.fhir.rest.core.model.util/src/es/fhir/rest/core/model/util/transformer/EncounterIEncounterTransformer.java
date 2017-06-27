package es.fhir.rest.core.model.util.transformer;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.LoggerFactory;

import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.model.util.transformer.helper.AbstractHelper;
import es.fhir.rest.core.model.util.transformer.helper.BehandlungHelper;
import es.fhir.rest.core.model.util.transformer.helper.FindingsContentHelper;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.KontaktService;

@Component
public class EncounterIEncounterTransformer implements IFhirTransformer<Encounter, IEncounter> {

	private FindingsContentHelper contentHelper = new FindingsContentHelper();

	private IFindingsService findingsService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFindingsService(IFindingsService findingsService) {
		this.findingsService = findingsService;
	}

	@Override
	public Optional<Encounter> getFhirObject(IEncounter localObject) {
		Optional<IBaseResource> resource = contentHelper.getResource(localObject);
		if (resource.isPresent()) {
			return Optional.of((Encounter) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public Optional<IEncounter> getLocalObject(Encounter fhirObject) {
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<IFinding> existing = findingsService.findById(fhirObject.getId(), IEncounter.class);
			if (existing.isPresent()) {
				return Optional.of((IEncounter) existing.get());
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<IEncounter> updateLocalObject(Encounter fhirObject, IEncounter localObject) {
		return Optional.empty();
	}

	@Override
	public Optional<IEncounter> createLocalObject(Encounter fhirObject) {
		// patient and performer must be present
		Optional<Kontakt> performerKontakt = KontaktService.load(BehandlungHelper.getMandatorId(fhirObject).get());
		Optional<Kontakt> patientKontakt = KontaktService.load(BehandlungHelper.getPatientId(fhirObject).get());
		if (performerKontakt.isPresent() && patientKontakt.isPresent()) {
			IEncounter iEncounter = findingsService.create(IEncounter.class);
			contentHelper.setResource(fhirObject, iEncounter);
			patientKontakt.ifPresent(k -> iEncounter.setPatientId(k.getId()));
			performerKontakt.ifPresent(k -> iEncounter.setMandatorId(k.getId()));
			Optional<Behandlung> behandlung = BehandlungHelper.createBehandlung(iEncounter);
			behandlung.ifPresent(cons -> {
				iEncounter.setConsultationId(cons.getId());
				AbstractHelper.acquireAndReleaseLock(cons);
			});
			findingsService.saveFinding(iEncounter);
			return Optional.of(iEncounter);
		} else {
			LoggerFactory.getLogger(EncounterIEncounterTransformer.class)
					.warn("Could not create encounter for mandator [" + performerKontakt + "] patient ["
							+ patientKontakt + "]");
		}
		return Optional.empty();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Encounter.class.equals(fhirClazz) && IEncounter.class.equals(localClazz);
	}

}
