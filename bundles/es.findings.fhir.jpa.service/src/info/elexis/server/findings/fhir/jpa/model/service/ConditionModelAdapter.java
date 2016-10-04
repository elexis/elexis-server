package info.elexis.server.findings.fhir.jpa.model.service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IEncounter;
import info.elexis.server.findings.fhir.jpa.model.annotated.Condition;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;

public class ConditionModelAdapter extends AbstractModelAdapter<Condition> implements ICondition {

	public ConditionModelAdapter(Condition model) {
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
		Optional<IBaseResource> resource = getFhirHelper().loadResource(this);
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Condition fhirCondition = (org.hl7.fhir.dstu3.model.Condition) resource.get();
			fhirCondition.setSubject(new Reference(new IdDt("Patient", patientId)));
		}
		getFhirHelper().saveResource(resource.get(), this);

		getModel().setPatientId(patientId);
	}

	@Override
	public List<ICoding> getCoding() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addCoding(ICoding coding) {
		// TODO Auto-generated method stub

	}

	@Override
	public Optional<LocalDateTime> getEffectiveTime() {
		Optional<IBaseResource> resource = getFhirHelper().loadResource(this);
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Condition fhirCondition = (org.hl7.fhir.dstu3.model.Condition) resource.get();
			Date date = fhirCondition.getDateRecorded();
			if (date != null) {
				return Optional.of(getLocalDateTime(date));
			}
		}
		return Optional.empty();
	}

	@Override
	public void setEffectiveTime(LocalDateTime time) {
		Optional<IBaseResource> resource = getFhirHelper().loadResource(this);
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Condition fhirCondition = (org.hl7.fhir.dstu3.model.Condition) resource.get();
			fhirCondition.setDateRecorded(getDate(time));
		}
		getFhirHelper().saveResource(resource.get(), this);
	}

	@Override
	public Optional<String> getText() {
		// TODO Auto-generated method stub
		return null;
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
		EncounterService encounterService = new EncounterService();
		Optional<Encounter> encounter = encounterService.findById(getModel().getEncounterId());
		if (encounter.isPresent()) {
			return Optional.of(new EncounterModelAdapter(encounter.get()));
		}
		return Optional.empty();
	}

	@Override
	public void setEncounter(IEncounter encounter) {
		getModel().setEncounterId(encounter.getId());
		setPatientId(encounter.getPatientId());
	}

}
