package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.migration.IMigratorService;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IModelService;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import es.fhir.rest.core.resources.util.DateRangeParamUtil;

@Component
public class EncounterResourceProvider implements IFhirResourceProvider {
	
	@Reference(target="("+IModelService.SERVICEMODELNAME+"=ch.elexis.core.model)")
	private IModelService modelService;
	
	@Reference
	private IFhirTransformerRegistry transformerRegistry;

	@Reference
	private IMigratorService migratorService;

	@Reference
	private IFindingsService findingsService;

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Encounter.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Encounter, IEncounter> getTransformer() {
		return (IFhirTransformer<Encounter, IEncounter>) transformerRegistry.getTransformerFor(Encounter.class,
				IEncounter.class);
	}

	@Read
	public Encounter getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<IEncounter> encounter = findingsService.findById(idPart, IEncounter.class);
			if (encounter.isPresent()) {
				Optional<Encounter> fhirEncounter = getTransformer().getFhirObject(encounter.get());
				return fhirEncounter.get();
			}
		}
		return null;
	}

	/**
	 * Search for all encounters by the patient id. Optional the date range of the
	 * returned encounters can be specified.
	 * 
	 * @param thePatientId
	 * @param dates
	 * @return
	 */
	@Search()
	public List<Encounter> findEncounter(@RequiredParam(name = Encounter.SP_PATIENT) IdType thePatientId,
			@OptionalParam(name = Encounter.SP_DATE) DateRangeParam dates) {
		if (thePatientId != null && !thePatientId.isEmpty()) {
			Optional<IPatient> patient = modelService.load(thePatientId.getIdPart(), IPatient.class);
			if (patient.isPresent()) {
				if (patient.get().isPatient()) {
					// migrate encounters first
					migratorService.migratePatientsFindings(thePatientId.getIdPart(), IEncounter.class, null);

					List<IEncounter> findings = findingsService.getPatientsFindings(patient.get().getId(),
							IEncounter.class);
					if (findings != null && !findings.isEmpty()) {
						List<Encounter> ret = new ArrayList<Encounter>();

						for (IEncounter iFinding : findings) {
							Optional<Encounter> fhirEncounter = getTransformer().getFhirObject(iFinding);
							fhirEncounter.ifPresent(fe -> {
								if (dates != null) {
									if (!DateRangeParamUtil.isPeriodInRange(fe.getPeriod(), dates)) {
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
	@Search()
	public List<Encounter> findEncounter(@RequiredParam(name = Encounter.SP_IDENTIFIER) TokenParam identifier) {
		if (identifier != null && !identifier.isEmpty() && identifier.getValue() != null
				&& !identifier.getValue().isEmpty()) {
			migratorService.migrateConsultationsFindings(identifier.getValue(), IEncounter.class);

			List<IEncounter> findings = findingsService.getConsultationsFindings(identifier.getValue(),
					IEncounter.class);
			if (findings != null && !findings.isEmpty()) {
				List<Encounter> ret = new ArrayList<Encounter>();
				for (IFinding iFinding : findings) {
					Optional<Encounter> fhirEncounter = getTransformer().getFhirObject((IEncounter) iFinding);
					fhirEncounter.ifPresent(fe -> ret.add(fe));
				}
				return ret;
			}
		}
		return null;
	}

	@Create
	public MethodOutcome createEncounter(@ResourceParam Encounter encounter) {
		MethodOutcome outcome = new MethodOutcome();
		Optional<IEncounter> exists = getTransformer().getLocalObject(encounter);
		if (exists.isPresent()) {
			outcome.setCreated(false);
			outcome.setId(new IdType(encounter.getId()));
		} else {
			Optional<IEncounter> created = getTransformer().createLocalObject(encounter);
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
