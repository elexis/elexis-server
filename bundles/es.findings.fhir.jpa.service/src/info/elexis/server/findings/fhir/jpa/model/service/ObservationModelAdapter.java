package info.elexis.server.findings.fhir.jpa.model.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.util.fhir.accessor.ObservationAccessor;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;
import info.elexis.server.findings.fhir.jpa.model.annotated.Observation;

public class ObservationModelAdapter extends AbstractModelAdapter<Observation> implements IObservation {

	private ObservationAccessor accessor = new ObservationAccessor();

	public ObservationModelAdapter(Observation model) {
		super(model);
	}

	@Override
	public String getId() {
		return getModel().getId();
	}

	@Override
	public String getPatientId() {
		return getModel().getPatientId();
	}

	@Override
	public void setPatientId(String patientId) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setPatientId((DomainResource) resource.get(), patientId);
			saveResource(resource.get());
		}

		getModel().setPatientId(patientId);
	}

	@Override
	public List<ICoding> getCoding() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getCoding((DomainResource) resource.get());
		}
		return Collections.emptyList();
	}

	@Override
	public void setCoding(List<ICoding> coding) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setCoding((DomainResource) resource.get(), coding);
			saveResource(resource.get());
		}
	}

	@Override
	public RawContentFormat getRawContentFormat() {
		return RawContentFormat.FHIR_JSON;
	}

	@Override
	public String getRawContent() {
		return getModel().getContent();
	}

	@Override
	public void setRawContent(String content) {
		getModel().setContent(content);
	}

	@Override
	public Optional<IEncounter> getEncounter() {
		String encounterId = getModel().getEncounterId();
		if (encounterId != null && !encounterId.isEmpty()) {
			EncounterService encounterService = new EncounterService();
			Optional<Encounter> encounterModel = encounterService.findById(encounterId);
			if (encounterModel.isPresent()) {
				return Optional.of(new EncounterModelAdapter(encounterModel.get()));
			}
		}
		return Optional.empty();
	}

	@Override
	public void setEncounter(IEncounter encounter) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Observation fhirObservation = (org.hl7.fhir.dstu3.model.Observation) resource
					.get();
			fhirObservation.setContext(new Reference(new IdDt("Encounter", encounter.getId())));

			saveResource(resource.get());
		}

		String patientId = encounter.getPatientId();
		if (patientId != null && !patientId.isEmpty() && getPatientId() == null) {
			setPatientId(patientId);
		}

		getModel().setEncounterId(encounter.getId());
	}

	@Override
	public List<IObservation> getSourceObservations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addSourceObservation(IObservation source) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<IObservation> getTargetObseravtions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addTargetObservation(IObservation source) {
		// TODO Auto-generated method stub

	}

	@Override
	public Optional<LocalDateTime> getEffectiveTime() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getEffectiveTime((DomainResource) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public void setEffectiveTime(LocalDateTime time) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setEffectiveTime((DomainResource) resource.get(), time);
			saveResource(resource.get());
		}
	}

	@Override
	public ObservationCategory getCategory() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getCategory((DomainResource) resource.get());
		}
		return ObservationCategory.UNKNOWN;
	}

	@Override
	public void setCategory(ObservationCategory category) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setCategory((DomainResource) resource.get(), category);
			saveResource(resource.get());
		}
	}
}
