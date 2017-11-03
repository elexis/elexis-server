package info.elexis.server.findings.fhir.jpa.model.service;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.findings.IClinicalImpression;
import ch.elexis.core.findings.IEncounter;
import info.elexis.server.findings.fhir.jpa.model.annotated.ClinicalImpression;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;

public class ClinicalImpressionModelAdapter extends AbstractModelAdapter<ClinicalImpression>
		implements IClinicalImpression {

	public ClinicalImpressionModelAdapter(ClinicalImpression model) {
		super(model);
	}

	@Override
	public String getId() {
		return getModel().getId();
	}

	@Override
	public String getPatientId() {
		return getModel().getPatientid();
	}

	@Override
	public String getRawContent() {
		return getModel().getContent();
	}

	@Override
	public RawContentFormat getRawContentFormat() {
		return RawContentFormat.FHIR_JSON;
	}

	@Override
	public void setPatientId(String patientId) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.ClinicalImpression fhirClinicalImpression = (org.hl7.fhir.dstu3.model.ClinicalImpression) resource
					.get();
			fhirClinicalImpression.setSubject(new Reference(new IdDt("Patient", patientId)));
			saveResource(resource.get());
		}

		getModel().setPatientid(patientId);
	}

	@Override
	public void setRawContent(String content) {
		getModel().setContent(content);
	}

	@Override
	public Optional<IEncounter> getEncounter() {
		String encounterId = getModel().getEncounterid();
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
			org.hl7.fhir.dstu3.model.ClinicalImpression fhirClinicalImpression = (org.hl7.fhir.dstu3.model.ClinicalImpression) resource
					.get();
			fhirClinicalImpression.setContext(new Reference(new IdDt("Encounter", encounter.getId())));

			saveResource(resource.get());
		}

		String patientId = encounter.getPatientId();
		if (patientId != null && !patientId.isEmpty() && getPatientId() == null) {
			setPatientId(patientId);
		}

		getModel().setEncounterid(encounter.getId());
	}
}
