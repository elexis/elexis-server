package info.elexis.server.findings.fhir.jpa.model.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ch.elexis.core.findings.ICoding;
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
			org.hl7.fhir.dstu3.model.Encounter fhirEncounter = (org.hl7.fhir.dstu3.model.Encounter) resource.get();
			Period period = fhirEncounter.getPeriod();
			if (period != null && period.getStart() != null) {
				return Optional.of(getLocalDateTime(period.getStart()));
			}
		}
		return Optional.empty();
	}

	@Override
	public void setEffectiveTime(LocalDateTime time) {
		Optional<IBaseResource> resource = getFhirHelper().loadResource(this);
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Encounter fhirEncounter = (org.hl7.fhir.dstu3.model.Encounter) resource.get();
			Period period = fhirEncounter.getPeriod();
			if (period == null) {
				period = new Period();
				period.setStart(getDate(time));
			} else {
				period.setStart(getDate(time));
			}
			fhirEncounter.setPeriod(period);
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
	public String getConsultationId() {
		return getModel().getConsultationId();
	}

	@Override
	public void setConsultationId(String consultationId) {
		getModel().setConsultationId(consultationId);
	}

	@Override
	public String getServiceProviderId() {
		return getModel().getServiceProviderId();
	}

	@Override
	public void setServiceProviderId(String serviceProviderId) {
		getModel().setServiceProviderId(serviceProviderId);
	}
}
