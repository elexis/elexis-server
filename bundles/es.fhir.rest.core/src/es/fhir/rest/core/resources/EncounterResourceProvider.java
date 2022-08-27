package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.migration.IMigratorService;
import ch.elexis.core.findings.util.DateRangeParamUtil;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.ILocalLockService;
import ch.elexis.core.services.IModelService;

@Component(service = IFhirResourceProvider.class)
public class EncounterResourceProvider
		extends AbstractFhirCrudResourceProvider<Encounter, IEncounter> {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;
	
	@Reference
	private ILocalLockService localLockService;
	
	@Reference
	private IFhirTransformerRegistry transformerRegistry;
	
	@Reference
	private IMigratorService migratorService;
	
	@Reference
	private IFindingsService findingsService;
	
	public EncounterResourceProvider(){
		super(IEncounter.class);
	}
	
	@Activate
	public void activate(){
		super.setCoreModelService(coreModelService);
		super.setLocalLockService(localLockService);
	}
	
	@Override
	public Class<? extends IBaseResource> getResourceType(){
		return Encounter.class;
	}
	
	@Override
	public IFhirTransformer<Encounter, IEncounter> getTransformer(){
		return (IFhirTransformer<Encounter, IEncounter>) transformerRegistry
			.getTransformerFor(Encounter.class, IEncounter.class);
	}
	
	/**
	 * Search for all encounters by the patient or subject id. Optional the date
	 * range of the returned encounters can be specified.
	 * 
	 * @param thePatientId
	 * @param dates
	 * @return
	 */
	@Search
	public List<Encounter> searchPatientOptDate(
			@OptionalParam(name = Encounter.SP_PATIENT) IdType thePatientId,
			@OptionalParam(name = Encounter.SP_SUBJECT) IdType theSubjectId,
		@OptionalParam(name = Encounter.SP_DATE) DateRangeParam dates, @IncludeParam(allow = {
			"Encounter.diagnosis"
		}) Set<Include> theIncludes){
		
		if (thePatientId == null && theSubjectId != null) {
			thePatientId = theSubjectId;
		}

		if (thePatientId != null && !thePatientId.isEmpty()) {
			Optional<IPatient> patient =
				coreModelService.load(thePatientId.getIdPart(), IPatient.class);
			if (patient.isPresent()) {
				if (patient.get().isPatient()) {
					// migrate encounters first
					migratorService.migratePatientsFindings(thePatientId.getIdPart(),
						IEncounter.class, null);
					
					List<IEncounter> findings = findingsService
						.getPatientsFindings(patient.get().getId(), IEncounter.class);
					if (findings != null && !findings.isEmpty()) {
						List<Encounter> ret = new ArrayList<Encounter>();
						
						for (IEncounter iFinding : findings) {
							Optional<Encounter> fhirEncounter =
								getTransformer().getFhirObject(iFinding);
							fhirEncounter.ifPresent(fe -> {
								if (dates != null) {
									if (!DateRangeParamUtil.isPeriodInRange(fe.getPeriod(),
										dates)) {
										return;
									}
								}
								ret.add(fe);
							});
						}
						return ret;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Search for an Encounter with a matching Elexis consultation id.
	 * 
	 * 
	 * @param identifier
	 * @return
	 */
	@Search
	public List<Encounter> searchReqIdentifier(
		@RequiredParam(name = Encounter.SP_IDENTIFIER) TokenParam identifier){
		if (identifier != null && !identifier.isEmpty() && identifier.getValue() != null
			&& !identifier.getValue().isEmpty()) {
			migratorService.migrateConsultationsFindings(identifier.getValue(), IEncounter.class);
			
			List<IEncounter> findings =
				findingsService.getConsultationsFindings(identifier.getValue(), IEncounter.class);
			if (findings != null && !findings.isEmpty()) {
				List<Encounter> ret = new ArrayList<Encounter>();
				for (IFinding iFinding : findings) {
					Optional<Encounter> fhirEncounter =
						getTransformer().getFhirObject((IEncounter) iFinding);
					fhirEncounter.ifPresent(fe -> ret.add(fe));
				}
				return ret;
			}
		}
		return null;
	}
	
}
