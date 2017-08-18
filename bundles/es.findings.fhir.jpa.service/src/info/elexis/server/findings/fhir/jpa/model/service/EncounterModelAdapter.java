package info.elexis.server.findings.fhir.jpa.model.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.util.fhir.accessor.EncounterAccessor;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;
import info.elexis.server.findings.fhir.jpa.service.FindingsService;

public class EncounterModelAdapter extends AbstractModelAdapter<Encounter> implements IEncounter {

	private EncounterAccessor accessor = new EncounterAccessor();

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
			accessor.setPatientId((DomainResource) resource.get(), patientId);
			saveResource(resource.get());
		}

		getModel().setPatientId(patientId);
	}

	@Override
	public Optional<LocalDateTime> getStartTime() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getStartTime((DomainResource) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public void setStartTime(LocalDateTime time) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setStartTime((DomainResource) resource.get(), time);
			saveResource(resource.get());
		}
	}

	@Override
	public Optional<LocalDateTime> getEndTime() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getEndTime((DomainResource) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public void setEndTime(LocalDateTime time) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setEndTime((DomainResource) resource.get(), time);
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
			accessor.setConsultationId((DomainResource) resource.get(), consultationId);
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
			accessor.setMandatorId((DomainResource) resource.get(), mandatorId);
			saveResource(resource.get());
		}

		getModel().setMandatorId(mandatorId);
	}

	public void setType(List<ICoding> coding) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setType((DomainResource) resource.get(), coding);
			saveResource(resource.get());
		}
	}

	public List<ICoding> getType() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getType((DomainResource) resource.get());
		}
		return Collections.emptyList();
	}

	@Override
	public List<ICondition> getIndication() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getIndication((DomainResource) resource.get(), new FindingsService());
		}
		return Collections.emptyList();
	}

	@Override
	public void setIndication(List<ICondition> indication) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setIndication((DomainResource) resource.get(), indication);
			saveResource(resource.get());
		}
	}
}
