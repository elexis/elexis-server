package info.elexis.server.findings.fhir.jpa.model.service;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ch.elexis.core.findings.IAllergyIntolerance;
import ch.elexis.core.findings.util.fhir.accessor.AllergyIntoleranceAccessor;
import info.elexis.server.findings.fhir.jpa.model.annotated.AllergyIntolerance;

public class AllergyIntoleranceModelAdapter extends AbstractModelAdapter<AllergyIntolerance>
		implements IAllergyIntolerance {

	private AllergyIntoleranceAccessor accessor = new AllergyIntoleranceAccessor();

	public AllergyIntoleranceModelAdapter(AllergyIntolerance model){
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
}
