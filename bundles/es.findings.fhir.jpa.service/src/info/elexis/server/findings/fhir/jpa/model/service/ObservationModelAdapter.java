package info.elexis.server.findings.fhir.jpa.model.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.util.ModelUtil;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;
import info.elexis.server.findings.fhir.jpa.model.annotated.Observation;

public class ObservationModelAdapter extends AbstractModelAdapter<Observation> implements IObservation {

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
			org.hl7.fhir.dstu3.model.Observation fhirObservation = (org.hl7.fhir.dstu3.model.Observation) resource
					.get();
			fhirObservation.setSubject(new Reference(new IdDt("Patient", patientId)));
			saveResource(resource.get());
		}

		getModel().setPatientId(patientId);
	}

	@Override
	public List<ICoding> getCoding() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Observation fhirObservation = (org.hl7.fhir.dstu3.model.Observation) resource
					.get();
			CodeableConcept codeableConcept = fhirObservation.getCode();
			if (codeableConcept != null) {
				return ModelUtil.getCodingsFromConcept(codeableConcept);
			}
		}
		return Collections.emptyList();
	}

	@Override
	public void setCoding(List<ICoding> coding) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Observation fhirObservation = (org.hl7.fhir.dstu3.model.Observation) resource
					.get();
			CodeableConcept codeableConcept = fhirObservation.getCode();
			if (codeableConcept == null) {
				codeableConcept = new CodeableConcept();
			}
			ModelUtil.setCodingsToConcept(codeableConcept, coding);
			fhirObservation.setCode(codeableConcept);
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
			fhirObservation.setEncounter(new Reference(new IdDt("Encounter", encounter.getId())));

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEffectiveTime(LocalDateTime time) {
		// TODO Auto-generated method stub

	}

	@Override
	public ObservationCategory getCategory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCategory(ObservationCategory category) {
		// TODO Auto-generated method stub

	}
}
