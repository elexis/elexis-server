package info.elexis.server.findings.fhir.jpa.model.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IEncounter;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;
import info.elexis.server.findings.fhir.jpa.model.util.ModelUtil;

public class EncounterModelAdapter extends AbstractModelAdapter<Encounter> implements IEncounter {

	public EncounterModelAdapter(Encounter model) {
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
			org.hl7.fhir.dstu3.model.Encounter fhirEncounter = (org.hl7.fhir.dstu3.model.Encounter) resource.get();
			fhirEncounter.setPatient(new Reference(new IdDt("Patient", patientId)));
			saveResource(resource.get());
		}

		getModel().setPatientId(patientId);
	}

	@Override
	public Optional<LocalDateTime> getStartTime() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Encounter fhirEncounter = (org.hl7.fhir.dstu3.model.Encounter) resource.get();
			Period period = fhirEncounter.getPeriod();
			if (period != null && period.getStart() != null) {
				return Optional.of(getLocalDateTime(period.getStart()));
			}
		}
		return Optional.empty();
	}

	@Override
	public void setStartTime(LocalDateTime time) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Encounter fhirEncounter = (org.hl7.fhir.dstu3.model.Encounter) resource.get();
			Period period = fhirEncounter.getPeriod();
			if (period == null) {
				period = new Period();
			}
			period.setStart(getDate(time));

			fhirEncounter.setPeriod(period);
			saveResource(resource.get());
		}
	}

	@Override
	public Optional<LocalDateTime> getEndTime() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Encounter fhirEncounter = (org.hl7.fhir.dstu3.model.Encounter) resource.get();
			Period period = fhirEncounter.getPeriod();
			if (period != null && period.getEnd() != null) {
				return Optional.of(getLocalDateTime(period.getEnd()));
			}
		}
		return Optional.empty();
	}

	@Override
	public void setEndTime(LocalDateTime time) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Encounter fhirEncounter = (org.hl7.fhir.dstu3.model.Encounter) resource.get();
			Period period = fhirEncounter.getPeriod();
			if (period == null) {
				period = new Period();
			}
			period.setEnd(getDate(time));

			fhirEncounter.setPeriod(period);
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
	public String getConsultationId() {
		return getModel().getConsultationId();
	}

	@Override
	public void setConsultationId(String consultationId) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Encounter fhirEncounter = (org.hl7.fhir.dstu3.model.Encounter) resource.get();
			boolean identifierFound = false;
			List<Identifier> existing = fhirEncounter.getIdentifier();
			for (Identifier existingIdentifier : existing) {
				if ("www.elexis.info/consultationid".equals(existingIdentifier.getSystem())) {
					existingIdentifier.setValue(consultationId);
					identifierFound = true;
					break;
				}
			}
			if (!identifierFound) {
				Identifier identifier = fhirEncounter.addIdentifier();
				identifier.setSystem("www.elexis.info/consultationid");
				identifier.setValue(consultationId);
			}
			saveResource(resource.get());
		}

		getModel().setConsultationId(consultationId);
	}

	@Override
	public String getMandatorId() {
		return getModel().getMandatorId();
	}

	@Override
	public void setMandatorId(String mandatorId) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Encounter fhirEncounter = (org.hl7.fhir.dstu3.model.Encounter) resource.get();
			EncounterParticipantComponent participant = new EncounterParticipantComponent();
			participant.setIndividual(new Reference("Practitioner/" + mandatorId));
			fhirEncounter.addParticipant(participant);
			saveResource(resource.get());
		}

		getModel().setMandatorId(mandatorId);
	}

	public void setType(List<ICoding> coding) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Encounter fhirEncounter = (org.hl7.fhir.dstu3.model.Encounter) resource.get();
			List<CodeableConcept> codeableConcepts = fhirEncounter.getType();
			if (!codeableConcepts.isEmpty()) {
				codeableConcepts.clear();
			}
			CodeableConcept codeableConcept = new CodeableConcept();
			ModelUtil.setCodingsToConcept(codeableConcept, coding);
			fhirEncounter.setType(Collections.singletonList(codeableConcept));
			saveResource(resource.get());
		}
	}

	public List<ICoding> getType() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Encounter fhirEncounter = (org.hl7.fhir.dstu3.model.Encounter) resource.get();
			List<CodeableConcept> codeableConcepts = fhirEncounter.getType();
			if (codeableConcepts != null) {
				ArrayList<ICoding> ret = new ArrayList<>();
				for (CodeableConcept codeableConcept : codeableConcepts) {
					ret.addAll(ModelUtil.getCodingsFromConcept(codeableConcept));
				}
				return ret;
			}
		}
		return Collections.emptyList();
	}

	@Override
	public List<ICondition> getIndication() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setIndication(List<ICondition> indication) {
		// TODO Auto-generated method stub

	}
}
