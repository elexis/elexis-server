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
import ch.elexis.core.findings.IProcedureRequest;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;
import info.elexis.server.findings.fhir.jpa.model.annotated.ProcedureRequest;
import info.elexis.server.findings.fhir.jpa.model.util.ModelUtil;

public class ProcedureRequestModelAdapter extends AbstractModelAdapter<ProcedureRequest> implements IProcedureRequest {

	public ProcedureRequestModelAdapter(ProcedureRequest model) {
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
			org.hl7.fhir.dstu3.model.ProcedureRequest fhirProcedureRequest = (org.hl7.fhir.dstu3.model.ProcedureRequest) resource
					.get();
			fhirProcedureRequest.setSubject(new Reference(new IdDt("Patient", patientId)));
			saveResource(resource.get());
		}

		getModel().setPatientId(patientId);
	}

	@Override
	public List<ICoding> getCoding() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.ProcedureRequest fhirProcedureRequest = (org.hl7.fhir.dstu3.model.ProcedureRequest) resource
					.get();
			CodeableConcept codeableConcept = fhirProcedureRequest.getCode();
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
			org.hl7.fhir.dstu3.model.ProcedureRequest fhirProcedureRequest = (org.hl7.fhir.dstu3.model.ProcedureRequest) resource
					.get();
			CodeableConcept codeableConcept = fhirProcedureRequest.getCode();
			if (codeableConcept == null) {
				codeableConcept = new CodeableConcept();
			}
			ModelUtil.setCodingsToConcept(codeableConcept, coding);
			fhirProcedureRequest.setCode(codeableConcept);
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
			org.hl7.fhir.dstu3.model.ProcedureRequest fhirProcedureRequest = (org.hl7.fhir.dstu3.model.ProcedureRequest) resource
					.get();
			fhirProcedureRequest.setEncounter(new Reference(new IdDt("Encounter", encounter.getId())));

			saveResource(resource.get());
		}

		String patientId = encounter.getPatientId();
		if (patientId != null && !patientId.isEmpty() && getPatientId() == null) {
			setPatientId(patientId);
		}

		getModel().setEncounterId(encounter.getId());
	}

	@Override
	public Optional<LocalDateTime> getScheduledTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setScheduledTime(LocalDateTime time) {
		// TODO Auto-generated method stub

	}
}
