package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ca.uhn.fhir.model.dstu.composite.IdentifierDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.KontaktService;

@Component
public class EncounterResourceProvider implements IFhirResourceProvider {

	private IFhirTransformer<Encounter, IEncounter> encounterMapper;

	private IFindingsService findingsService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFindingsService(IFindingsService findingsService) {
		this.findingsService = findingsService;
	}

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Encounter.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initTransformer(IFhirTransformerRegistry transformerRegistry) {
		encounterMapper = (IFhirTransformer<Encounter, IEncounter>) transformerRegistry
				.getTransformerFor(Encounter.class,
						IEncounter.class);
		if (encounterMapper == null) {
			throw new IllegalStateException("No transformer available");
		}
	}

	@Read
	public Encounter getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<IFinding> encounter = findingsService.findById(idPart);
			if (encounter.isPresent() && (encounter.get() instanceof IEncounter)) {
				Optional<Encounter> fhirEncounter = encounterMapper.getFhirObject((IEncounter) encounter.get());
				return fhirEncounter.get();
			}
		}
		return null;
	}

	@Search()
	public List<Encounter> findEncounter(@RequiredParam(name = Encounter.SP_PATIENT) IdType thePatientId) {
		if (thePatientId != null && !thePatientId.isEmpty()) {
			Optional<Kontakt> patient = KontaktService.INSTANCE.findById(thePatientId.getIdPart());
			if (patient.isPresent()) {
				if (patient.get().isPatient()) {
					List<IFinding> findings = findingsService.getPatientsFindings(patient.get().getId(),
							IEncounter.class);
					if (findings != null && !findings.isEmpty()) {
						List<Encounter> ret = new ArrayList<Encounter>();
						for (IFinding iFinding : findings) {
							Optional<Encounter> fhirEncounter = encounterMapper.getFhirObject((IEncounter) iFinding);
							fhirEncounter.ifPresent(fe -> ret.add(fe));
						}
						return ret;
					}
				}
			}
		}
		return null;
	}

	@Search()
	public List<Encounter> findEncounter(@RequiredParam(name = Patient.SP_IDENTIFIER) IdentifierDt identifier) {
		if (identifier != null && !identifier.isEmpty() && identifier.getValue() != null
				&& !identifier.getValue().isEmpty()) {
			List<IFinding> findings = findingsService.getConsultationsFindings(identifier.getValue().getValue(),
					IEncounter.class);
			if (findings != null && !findings.isEmpty()) {
				List<Encounter> ret = new ArrayList<Encounter>();
				for (IFinding iFinding : findings) {
					Optional<Encounter> fhirEncounter = encounterMapper.getFhirObject((IEncounter) iFinding);
					fhirEncounter.ifPresent(fe -> ret.add(fe));
				}
				return ret;
			}
		}
		return null;
	}
}
