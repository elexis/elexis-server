package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ca.uhn.fhir.model.dstu.composite.IdentifierDt;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.migration.IMigratorService;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import es.fhir.rest.core.resources.util.DateRangeParamUtil;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.KontaktService;

@Component
public class EncounterResourceProvider implements IFhirResourceProvider {

	private IMigratorService migratorService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIMigratorService(IMigratorService migratorService) {
		this.migratorService = migratorService;
	}

	private IFindingsService findingsService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFindingsService(IFindingsService findingsService) {
		this.findingsService = findingsService;
	}

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Encounter.class;
	}

	private IFhirTransformerRegistry transformerRegistry;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFhirTransformerRegistry(IFhirTransformerRegistry transformerRegistry) {
		this.transformerRegistry = transformerRegistry;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Encounter, IEncounter> getTransformer() {
		return (IFhirTransformer<Encounter, IEncounter>) transformerRegistry
					.getTransformerFor(Encounter.class, IEncounter.class);
	}

	@Read
	public Encounter getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<IFinding> encounter = findingsService.findById(idPart);
			if (encounter.isPresent() && (encounter.get() instanceof IEncounter)) {
				Optional<Encounter> fhirEncounter = getTransformer().getFhirObject((IEncounter) encounter.get());
				return fhirEncounter.get();
			}
		}
		return null;
	}

	/**
	 * Search for all encounters by the patient id. Optional the date range of
	 * the returned encounters can be specified.
	 * 
	 * @param thePatientId
	 * @param dates
	 * @return
	 */
	@Search()
	public List<Encounter> findEncounter(@RequiredParam(name = Encounter.SP_PATIENT) IdType thePatientId,
			@OptionalParam(name = Encounter.SP_DATE) DateRangeParam dates) {
		if (thePatientId != null && !thePatientId.isEmpty()) {
			Optional<Kontakt> patient = KontaktService.INSTANCE.findById(thePatientId.getIdPart());
			if (patient.isPresent()) {
				if (patient.get().isPatient()) {
					// migrate encounters first
					migratorService.migratePatientsFindings(thePatientId.getIdPart(), IEncounter.class);

					List<IFinding> findings = findingsService.getPatientsFindings(patient.get().getId(),
							IEncounter.class);
					if (findings != null && !findings.isEmpty()) {
						List<Encounter> ret = new ArrayList<Encounter>();

						for (IFinding iFinding : findings) {
							Optional<Encounter> fhirEncounter = getTransformer().getFhirObject((IEncounter) iFinding);
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
	public List<Encounter> findEncounter(@RequiredParam(name = Encounter.SP_IDENTIFIER) IdentifierDt identifier) {
		if (identifier != null && !identifier.isEmpty() && identifier.getValue() != null
				&& !identifier.getValue().isEmpty()) {
			migratorService.migrateConsultationsFindings(identifier.getValue().getValue(), IEncounter.class);

			List<IFinding> findings = findingsService.getConsultationsFindings(identifier.getValue().getValue(),
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
