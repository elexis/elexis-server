package es.fhir.rest.core.model.util.transformer;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.IProcedureRequest;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.model.util.transformer.helper.FindingsContentHelper;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.KontaktService;

@Component(immediate = true)
public class ProcedureRequestIProcedureRequestTransformer
		implements IFhirTransformer<ProcedureRequest, IProcedureRequest> {

	private FindingsContentHelper contentHelper = new FindingsContentHelper();

	private IFindingsService findingsService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFindingsService(IFindingsService findingsService) {
		this.findingsService = findingsService;
	}

	@Override
	public Optional<ProcedureRequest> getFhirObject(IProcedureRequest localObject) {
		Optional<IBaseResource> resource = contentHelper.getResource(localObject);
		if (resource.isPresent()) {
			return Optional.of((ProcedureRequest) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public Optional<IProcedureRequest> getLocalObject(ProcedureRequest fhirObject) {
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<IFinding> existing = findingsService.findById(fhirObject.getId(), IProcedureRequest.class);
			if (existing.isPresent()) {
				return Optional.of((IProcedureRequest) existing.get());
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<IProcedureRequest> updateLocalObject(ProcedureRequest fhirObject, IProcedureRequest localObject) {
		return Optional.empty();
	}

	@Override
	public Optional<IProcedureRequest> createLocalObject(ProcedureRequest fhirObject) {
		IProcedureRequest iProcedureRequest = findingsService.getFindingsFactory().createProcedureRequest();
		contentHelper.setResource(fhirObject, iProcedureRequest);
		if (fhirObject.getSubject() != null && fhirObject.getSubject().hasReference()) {
			String id = fhirObject.getSubject().getReferenceElement().getIdPart();
			Optional<Kontakt> patient = KontaktService.INSTANCE.findById(id);
			patient.ifPresent(k -> iProcedureRequest.setPatientId(id));
		}
		if (fhirObject.getEncounter() != null && fhirObject.getEncounter().hasReference()) {
			String id = fhirObject.getEncounter().getReferenceElement().getIdPart();
			Optional<IFinding> encounter = findingsService.findById(id, IEncounter.class);
			encounter.ifPresent(e -> iProcedureRequest.setEncounter((IEncounter) e));
		}
		findingsService.saveFinding(iProcedureRequest);
		return Optional.of(iProcedureRequest);
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return ProcedureRequest.class.equals(fhirClazz) && IProcedureRequest.class.equals(localClazz);
	}

}
