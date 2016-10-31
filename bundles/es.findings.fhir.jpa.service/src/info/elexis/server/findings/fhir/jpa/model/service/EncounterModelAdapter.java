package info.elexis.server.findings.fhir.jpa.model.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IEncounter;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;

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
	public Optional<String> getText() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Encounter fhirEncounter = (org.hl7.fhir.dstu3.model.Encounter) resource.get();
			if (fhirEncounter.hasText()) {
				Narrative narrative = fhirEncounter.getText();
				return Optional.of(narrative.getDivAsString());
			}
		}
		return Optional.empty();
	}

	public void setText(String text) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Encounter fhirEncounter = (org.hl7.fhir.dstu3.model.Encounter) resource.get();
			Narrative narrative;
			if (fhirEncounter.hasText()) {
				narrative = fhirEncounter.getText();
			} else {
				narrative = new Narrative();
			}
			text = text.replaceAll("(\r\n|\r|\n)", "<br />");
			narrative.setDivAsString(text);
			fhirEncounter.setText(narrative);
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
	public String getServiceProviderId() {
		return getModel().getServiceProviderId();
	}

	@Override
	public void setServiceProviderId(String serviceProviderId) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Encounter fhirEncounter = (org.hl7.fhir.dstu3.model.Encounter) resource.get();
			fhirEncounter.setServiceProvider(new Reference(new IdDt("Practitioner", serviceProviderId)));
			saveResource(resource.get());
		}

		getModel().setServiceProviderId(serviceProviderId);
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
