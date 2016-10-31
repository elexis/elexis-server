package info.elexis.server.findings.fhir.jpa.model.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.findings.IFinding;
import info.elexis.server.findings.fhir.jpa.model.util.ModelUtil;

public abstract class AbstractModelAdapter<T> implements IFinding {

	private T model;

	public AbstractModelAdapter(T model) {
		this.model = model;
	}

	public T getModel() {
		return model;
	}

	protected Logger getLogger(Class<?> clazz) {
		return LoggerFactory.getLogger(clazz);
	}

	protected void saveResource(IBaseResource resource) {
		ModelUtil.saveResource(resource, this);
	}

	protected Optional<IBaseResource> loadResource() {
		return ModelUtil.loadResource(this);
	}

	@Override
	public Optional<String> getText() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent() && resource.get() instanceof DomainResource) {
			Narrative narrative = ((DomainResource) resource.get()).getText();
			if (narrative != null && narrative.getDivAsString() != null) {
				String text = narrative.getDivAsString();
				if (text != null) {
					String divDecodedText = text
							.replaceAll("<div>|<div xmlns=\"http://www.w3.org/1999/xhtml\">|</div>|</ div>", "");
					divDecodedText = divDecodedText.replaceAll("<br/>|<br />", "\n");
					return Optional.of(divDecodedText);
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public void setText(String text) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent() && resource.get() instanceof DomainResource) {
			DomainResource domainResource = (DomainResource) resource.get();
			Narrative narrative = domainResource.getText();
			if (narrative == null) {
				narrative = new Narrative();
			}
			String divEncodedText = text.replaceAll("(\r\n|\r|\n)", "<br />");
			narrative.setDivAsString(divEncodedText);
			domainResource.setText(narrative);
			saveResource(domainResource);
		}
	}

	public void addStringExtension(String theUrl, String theValue) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent() && resource.get() instanceof DomainResource) {
			DomainResource domainResource = (DomainResource) resource.get();
			Extension extension = new Extension(theUrl);
			extension.setValue(new StringType().setValue(theValue));
			domainResource.addExtension(extension);
			saveResource(domainResource);
		}
	}

	public Map<String, String> getStringExtensions() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent() && resource.get() instanceof DomainResource) {
			List<Extension> extensions = ((DomainResource) resource.get()).getExtension();
			return extensions.stream().filter(extension -> extension.getValue() instanceof StringType)
					.collect(Collectors.toMap(extension -> extension.getUrl(),
							extension -> ((StringType) extension.getValue()).getValueAsString()));
		}
		return Collections.emptyMap();
	}
}
