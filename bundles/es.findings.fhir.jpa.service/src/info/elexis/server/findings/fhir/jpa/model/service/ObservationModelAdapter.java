package info.elexis.server.findings.fhir.jpa.model.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import ch.elexis.core.findings.IObservationLink.ObservationLinkType;
import ch.elexis.core.findings.ObservationComponent;
import ch.elexis.core.findings.scripting.FindingsScriptingUtil;
import ch.elexis.core.findings.util.fhir.accessor.ObservationAccessor;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;
import info.elexis.server.findings.fhir.jpa.model.annotated.Observation;
import info.elexis.server.findings.fhir.jpa.model.annotated.ObservationLink;
import info.elexis.server.findings.fhir.jpa.model.annotated.ObservationLink_;
import info.elexis.server.findings.fhir.jpa.model.service.JPAQuery.QUERY;

public class ObservationModelAdapter extends AbstractModelAdapter<Observation> implements IObservation {

	private static final String FORMAT_KEY_VALUE_SPLITTER = ":-:";
	private static final String FORMAT_SPLITTER = ":split:";

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

	@Override
	public List<IObservation> getSourceObservations(ObservationLinkType type) {
		ObservationService observationService = new ObservationService();
		JPAQuery<ObservationLink> query = new JPAQuery<>(ObservationLink.class);
		query.add(ObservationLink_.targetid, QUERY.EQUALS, getId());
		query.add(ObservationLink_.type, QUERY.EQUALS, type.name());

		List<ObservationLink> links = query.execute();
		List<IObservation> iObservations = new ArrayList<>();
		for (ObservationLink link : links) {
			Optional<Observation> observation = observationService.findById(link.getSourceid());
			observation.ifPresent(o -> iObservations.add(new ObservationModelAdapter(o)));
		}
		return iObservations;
	}

	@Override
	public void addSourceObservation(IObservation source, ObservationLinkType type) {
		if (source != null && source.getId() != null && getId() != null) {
			ObservationLinkService linkService = new ObservationLinkService();
			ObservationLink observationLink = linkService.create();
			observationLink.setTargetid(getId());
			observationLink.setSourceid(source.getId());
			observationLink.setType(type.name());
			linkService.write(observationLink);
		}
	}

	@Override
	public void removeSourceObservation(IObservation source, ObservationLinkType type) {
		JPAQuery<ObservationLink> qbe = new JPAQuery<>(ObservationLink.class);
		qbe.add(ObservationLink_.targetid, QUERY.EQUALS, getId());
		qbe.add(ObservationLink_.sourceid, QUERY.EQUALS, source.getId());
		qbe.add(ObservationLink_.type, QUERY.EQUALS, type.name());

		List<ObservationLink> observationLinks = qbe.execute();
		ObservationLinkService linkService = new ObservationLinkService();
		for (ObservationLink link : observationLinks) {
			linkService.delete(link);
		}
	}

	@Override
	public List<IObservation> getTargetObseravtions(ObservationLinkType type) {
		ObservationService observationService = new ObservationService();
		JPAQuery<ObservationLink> query = new JPAQuery<>(ObservationLink.class);
		query.add(ObservationLink_.sourceid, QUERY.EQUALS, getId());
		query.add(ObservationLink_.type, QUERY.EQUALS, type.name());

		List<ObservationLink> links = query.execute();
		List<IObservation> iObservations = new ArrayList<>();
		for (ObservationLink link : links) {
			Optional<Observation> observation = observationService.findById(link.getTargetid());
			observation.ifPresent(o -> iObservations.add(new ObservationModelAdapter(o)));
		}
		return iObservations;
	}

	@Override
	public void addTargetObservation(IObservation target, ObservationLinkType type) {
		if (target != null && target.getId() != null && getId() != null) {
			ObservationLinkService linkService = new ObservationLinkService();
			ObservationLink observationLink = linkService.create();
			observationLink.setTargetid(target.getId());
			observationLink.setSourceid(getId());
			observationLink.setType(type.name());
			linkService.write(observationLink);
		}
	}

	@Override
	public void removeTargetObservation(IObservation target, ObservationLinkType type) {
		JPAQuery<ObservationLink> qbe = new JPAQuery<>(ObservationLink.class);
		qbe.add(ObservationLink_.sourceid, QUERY.EQUALS, getId());
		qbe.add(ObservationLink_.targetid, QUERY.EQUALS, target.getId());
		qbe.add(ObservationLink_.type, QUERY.EQUALS, type.name());

		List<ObservationLink> observationLinks = qbe.execute();
		ObservationLinkService linkService = new ObservationLinkService();
		for (ObservationLink link : observationLinks) {
			linkService.delete(link);
		}
	}

	@Override
	public void addComponent(ObservationComponent component) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.addComponent((DomainResource) resource.get(), component);
			saveResource(resource.get());
		}
	}

	@Override
	public void updateComponent(ObservationComponent component) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.updateComponent((DomainResource) resource.get(), component);
			saveResource(resource.get());
		}
	}

	@Override
	public List<ObservationComponent> getComponents() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getComponents((DomainResource) resource.get());
		}
		return Collections.emptyList();
	}

	@Override
	public void setNumericValue(BigDecimal bigDecimal, String unit) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setNumericValue((DomainResource) resource.get(), bigDecimal, unit);
			saveResource(resource.get());
		}
	}

	@Override
	public Optional<BigDecimal> getNumericValue() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			if (FindingsScriptingUtil.hasScript(this)) {
				FindingsScriptingUtil.evaluate(this);
				resource = loadResource();
			}
			return accessor.getNumericValue((DomainResource) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public void setStringValue(String value) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setStringValue((DomainResource) resource.get(), value);
			saveResource(resource.get());
		}
	}

	@Override
	public Optional<String> getStringValue() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getStringValue((DomainResource) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public Optional<String> getNumericValueUnit() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getNumericValueUnit((DomainResource) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public void setObservationType(ObservationType observationType) {
		if (observationType != null) {
			getModel().setType(observationType.name());
		}
	}

	@Override
	public ObservationType getObservationType() {
		String type = getModel().getType();
		return type != null ? ObservationType.valueOf(type) : null;
	}

	@Override
	public boolean isReferenced() {
		return getModel().isReferenced();
	}

	@Override
	public void setReferenced(boolean referenced) {
		getModel().setReferenced(referenced);
	}

	@Override
	public void setComment(String comment) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setComment((DomainResource) resource.get(), comment);
			saveResource(resource.get());
		}
	}

	@Override
	public Optional<String> getComment() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getComment((DomainResource) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public void addFormat(String key, String value) {
		StringBuilder builder = new StringBuilder(getModel().getFormat());
		String dbValue = getFormat(key);
		String dbKeyValue = key + FORMAT_KEY_VALUE_SPLITTER + dbValue;

		int idx = builder.indexOf(dbKeyValue);
		if (idx == -1) {
			if (builder.length() > 0) {
				builder.append(FORMAT_SPLITTER);
			}
			builder.append(key + FORMAT_KEY_VALUE_SPLITTER + value);
		} else {
			builder.replace(idx, idx + dbKeyValue.length(), key + FORMAT_KEY_VALUE_SPLITTER + value);
		}
		getModel().setFormat(builder.toString());
	}

	@Override
	public String getFormat(String key) {
		String format = getModel().getFormat();
		if (format != null && format.contains(key + FORMAT_KEY_VALUE_SPLITTER)) {
			String[] splits = format.split(key + FORMAT_KEY_VALUE_SPLITTER);
			if (splits.length > 1) {
				return splits[1].split(FORMAT_SPLITTER)[0];
			}
		}
		return "";
	}

	@Override
	public Optional<String> getScript() {
		String value = getModel().getScript();
		if (value != null && !value.isEmpty()) {
			return Optional.of(value);
		}
		return Optional.empty();
	}

	@Override
	public void setScript(String script) {
		getModel().setScript(script);
	}

	@Override
	public int getDecimalPlace() {
		String value = getModel().getDecimalplace();
		if (value != null && !value.isEmpty()) {
			return Integer.valueOf(value);
		}
		return -1;
	}

	@Override
	public void setDecimalPlace(int value) {
		getModel().setDecimalplace(Integer.toString(value));
	}

	@Override
	public Optional<String> getOriginUri() {
		String value = getModel().getOriginuri();
		if (value != null && !value.isEmpty()) {
			return Optional.of(value);
		}
		return Optional.empty();
	}

	@Override
	public void setOriginUri(String uri) {
		getModel().setOriginuri(uri);
	}
}
