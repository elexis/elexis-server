package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.FamilyMemberHistory;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ch.elexis.core.findings.IFamilyMemberHistory;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.KontaktService;

@Component
public class FamilyMemberHistoryResourceProvider implements IFhirResourceProvider {

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return FamilyMemberHistory.class;
	}

	private IFhirTransformerRegistry transformerRegistry;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFhirTransformerRegistry(IFhirTransformerRegistry transformerRegistry) {
		this.transformerRegistry = transformerRegistry;
	}

	private IFindingsService findingsService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFindingsService(IFindingsService findingsService) {
		this.findingsService = findingsService;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<FamilyMemberHistory, IFamilyMemberHistory> getTransformer(){
		return (IFhirTransformer<FamilyMemberHistory, IFamilyMemberHistory>) transformerRegistry
			.getTransformerFor(FamilyMemberHistory.class, IFamilyMemberHistory.class);
	}

	@Search()
	public List<FamilyMemberHistory> findFamilyMemberHistory(
		@RequiredParam(name = FamilyMemberHistory.SP_PATIENT) IdType patientId){
		if (patientId != null && !patientId.isEmpty()) {
			Optional<Kontakt> patient = KontaktService.load(patientId.getIdPart());
			if (patient.isPresent()) {
				if (patient.get().isPatient()) {
					List<FamilyMemberHistory> ret = new ArrayList<>();
					List<IFinding> findings = findingsService
						.getPatientsFindings(patientId.getIdPart(), IFamilyMemberHistory.class);
					if (findings != null && !findings.isEmpty()) {
						for (IFinding iFinding : findings) {
							
							Optional<FamilyMemberHistory> fhirFamilyMemberHistory =
								getTransformer().getFhirObject((IFamilyMemberHistory) iFinding);
							if (fhirFamilyMemberHistory.isPresent()) {
								ret.add(fhirFamilyMemberHistory.get());
							}
						}
					}
					return ret;
				}
			}
		}
		return Collections.emptyList();
	}
	
	@Create
	public MethodOutcome createFamilyMemberHistory(
		@ResourceParam FamilyMemberHistory familyMemberHistory){
		MethodOutcome outcome = new MethodOutcome();
		
		Optional<IFamilyMemberHistory> exists =
			getTransformer().getLocalObject(familyMemberHistory);
		if (exists.isPresent()) {
			outcome.setCreated(false);
			outcome.setId(new IdType(familyMemberHistory.getId()));
		} else {
			Optional<IFamilyMemberHistory> created =
				getTransformer().createLocalObject(familyMemberHistory);
			if (created.isPresent()) {
				outcome.setCreated(true);
				outcome.setId(new IdType(created.get().getId()));
			} else {
				throw new InternalErrorException("Creation failed");
			}
		}
		return outcome;
	}
}
