package info.elexis.server.findings.fhir.jpa.model.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition.ConditionClinicalStatus;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IEncounter;
import info.elexis.server.findings.fhir.jpa.model.annotated.Condition;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;

public class ConditionModelAdapter extends AbstractModelAdapter<Condition> implements ICondition {

	private EnumMapping categoryMapping = new EnumMapping(
			org.hl7.fhir.instance.model.valuesets.ConditionCategory.class,
			ch.elexis.core.findings.ICondition.ConditionCategory.class);
	private EnumMapping statusMapping = new EnumMapping(ConditionClinicalStatus.class,
			ch.elexis.core.findings.ICondition.ConditionStatus.class);
	
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
		Optional<IBaseResource> resource = getFhirHelper().loadResource(this);
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Condition fhirCondition = (org.hl7.fhir.dstu3.model.Condition) resource.get();
			CodeableConcept codeableConcept = fhirCondition.getCode();
			if (codeableConcept != null) {

			}
		}
		return Collections.emptyList();
	}

	@Override
	public void addCoding(ICoding coding) {
		Optional<IBaseResource> resource = getFhirHelper().loadResource(this);
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Condition fhirCondition = (org.hl7.fhir.dstu3.model.Condition) resource.get();
			CodeableConcept codeableConcept = fhirCondition.getCode();
			if(codeableConcept == null) {
				codeableConcept = new CodeableConcept();
			}
			addCodingToConcept(codeableConcept, coding);
			fhirCondition.setCode(codeableConcept);
			getFhirHelper().saveResource(resource.get(), this);
		}
	}

	private void addCodingToConcept(CodeableConcept codeableConcept, ICoding coding) {
		// check if it is already contained in the concept
		List<Coding> conceptCoding = codeableConcept.getCoding();
		for (Coding conceptCode : conceptCoding) {
			if (conceptCode.getSystem().equals(coding.getSystem()) && conceptCode.getCode().equals(coding.getCode())) {
				return;
			}
		}
		codeableConcept.addCoding(new Coding(coding.getSystem(), coding.getCode(), coding.getDisplay()));
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

	@Override
	public ConditionCategory getCategory() {
		Optional<IBaseResource> resource = getFhirHelper().loadResource(this);
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Condition fhirCondition = (org.hl7.fhir.dstu3.model.Condition) resource.get();
			List<Coding> coding = fhirCondition.getCategory().getCoding();
			if (!coding.isEmpty()) {
				for (Coding categoryCoding : coding) {
					if (categoryCoding.getSystem().equals("http://hl7.org/fhir/condition-category")) {
						return (ConditionCategory) categoryMapping.getLocalEnumValueByCode(categoryCoding.getCode());
					}
				}
			}
		}
		return ConditionCategory.UNKNOWN;
	}

	@Override
	public void setCategory(ConditionCategory category) {
		Optional<IBaseResource> resource = getFhirHelper().loadResource(this);
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Condition fhirCondition = (org.hl7.fhir.dstu3.model.Condition) resource.get();
			CodeableConcept categoryCode = new CodeableConcept();
			org.hl7.fhir.instance.model.valuesets.ConditionCategory fhirCategoryCode = (org.hl7.fhir.instance.model.valuesets.ConditionCategory) categoryMapping
					.getFhirEnumValueByEnum(category);
			if (fhirCategoryCode != null) {
				categoryCode.setCoding(Collections.singletonList(new Coding(fhirCategoryCode.getSystem(),
						fhirCategoryCode.toCode(), fhirCategoryCode.getDisplay())));
				fhirCondition.setCategory(categoryCode);
			}
		}
		getFhirHelper().saveResource(resource.get(), this);
	}

	@Override
	public ConditionStatus getStatus() {
		Optional<IBaseResource> resource = getFhirHelper().loadResource(this);
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Condition fhirCondition = (org.hl7.fhir.dstu3.model.Condition) resource.get();
			ConditionClinicalStatus fhirStatus = fhirCondition.getClinicalStatus();
			if (fhirStatus != null) {
				return (ConditionStatus) statusMapping.getLocalEnumValueByCode(fhirStatus.toCode());
			}
		}
		return ConditionStatus.UNKNOWN;
	}

	@Override
	public void setStatus(ConditionStatus status) {
		Optional<IBaseResource> resource = getFhirHelper().loadResource(this);
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Condition fhirCondition = (org.hl7.fhir.dstu3.model.Condition) resource.get();
			ConditionClinicalStatus fhirCategoryCode = (ConditionClinicalStatus) statusMapping
					.getFhirEnumValueByEnum(status);
			if (fhirCategoryCode != null) {
				fhirCondition.setClinicalStatus(fhirCategoryCode);
			}
		}
		getFhirHelper().saveResource(resource.get(), this);
	}
}
