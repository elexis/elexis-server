package info.elexis.server.findings.fhir.jpa.model.service;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ch.elexis.core.findings.IFamilyMemberHistory;
import ch.elexis.core.findings.util.fhir.accessor.FamilyMemberHistoryAccessor;
import info.elexis.server.findings.fhir.jpa.model.annotated.FamilyMemberHistory;

public class FamilyMemberHistoryModelAdapter extends AbstractModelAdapter<FamilyMemberHistory>
		implements IFamilyMemberHistory {

	private FamilyMemberHistoryAccessor accessor = new FamilyMemberHistoryAccessor();

	public FamilyMemberHistoryModelAdapter(FamilyMemberHistory model){
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
