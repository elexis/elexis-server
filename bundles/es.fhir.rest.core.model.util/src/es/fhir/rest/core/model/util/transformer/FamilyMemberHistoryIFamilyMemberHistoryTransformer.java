package es.fhir.rest.core.model.util.transformer;

import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.dstu3.model.FamilyMemberHistory;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ca.uhn.fhir.model.api.Include;
import ch.elexis.core.findings.IFamilyMemberHistory;
import ch.elexis.core.findings.IFindingsService;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.model.util.transformer.helper.FindingsContentHelper;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.KontaktService;

@Component(immediate = true)
public class FamilyMemberHistoryIFamilyMemberHistoryTransformer
		implements IFhirTransformer<FamilyMemberHistory, IFamilyMemberHistory> {

	private FindingsContentHelper contentHelper = new FindingsContentHelper();

	private IFindingsService findingsService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFindingsService(IFindingsService findingsService) {
		this.findingsService = findingsService;
	}

	@Override
	public Optional<FamilyMemberHistory> getFhirObject(IFamilyMemberHistory localObject, Set<Include> includes){
		Optional<IBaseResource> resource = contentHelper.getResource(localObject);
		if (resource.isPresent()) {
			return Optional.of((FamilyMemberHistory) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public Optional<IFamilyMemberHistory> getLocalObject(FamilyMemberHistory fhirObject){
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<IFamilyMemberHistory> existing =
				findingsService.findById(fhirObject.getId(), IFamilyMemberHistory.class);
			if (existing.isPresent()) {
				return Optional.of(existing.get());
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<IFamilyMemberHistory> updateLocalObject(FamilyMemberHistory fhirObject,
		IFamilyMemberHistory localObject){
		return Optional.empty();
	}

	@Override
	public Optional<IFamilyMemberHistory> createLocalObject(FamilyMemberHistory fhirObject){
		IFamilyMemberHistory IFamilyMemberHistory =
			findingsService.create(IFamilyMemberHistory.class);
		contentHelper.setResource(fhirObject, IFamilyMemberHistory);
		if (fhirObject.getPatient() != null && fhirObject.getPatient().hasReference()) {
			String id = fhirObject.getPatient().getReferenceElement().getIdPart();
			Optional<Kontakt> patient = KontaktService.load(id);
			patient.ifPresent(k -> IFamilyMemberHistory.setPatientId(id));
		}
		findingsService.saveFinding(IFamilyMemberHistory);
		return Optional.of(IFamilyMemberHistory);
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return FamilyMemberHistory.class.equals(fhirClazz)
			&& IFamilyMemberHistory.class.equals(localClazz);
	}

}
