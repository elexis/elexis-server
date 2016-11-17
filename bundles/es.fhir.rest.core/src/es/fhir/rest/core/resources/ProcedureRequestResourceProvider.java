package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.IProcedureRequest;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.KontaktService;

@Component
public class ProcedureRequestResourceProvider implements IFhirResourceProvider {

	private IFindingsService findingsService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFindingsService(IFindingsService findingsService) {
		this.findingsService = findingsService;
	}

	private IFhirTransformerRegistry transformerRegistry;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFhirTransformerRegistry(IFhirTransformerRegistry transformerRegistry) {
		this.transformerRegistry = transformerRegistry;
	}

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return ProcedureRequest.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<ProcedureRequest, IProcedureRequest> getTransformer() {
		return (IFhirTransformer<ProcedureRequest, IProcedureRequest>) transformerRegistry
				.getTransformerFor(ProcedureRequest.class, IProcedureRequest.class);
	}

	@Read
	public ProcedureRequest getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<IFinding> procedureRequest = findingsService.findById(idPart);
			if (procedureRequest.isPresent() && (procedureRequest.get() instanceof IProcedureRequest)) {
				Optional<ProcedureRequest> fhirProcedureRequest = getTransformer()
						.getFhirObject((IProcedureRequest) procedureRequest.get());
				return fhirProcedureRequest.get();
			}
		}
		return null;
	}

	@Search()
	public List<ProcedureRequest> findProcedureRequest(
			@RequiredParam(name = ProcedureRequest.SP_PATIENT) IdType thePatientId,
			@OptionalParam(name = ProcedureRequest.SP_ENCOUNTER) IdType theEncounterId) {
		if (thePatientId != null && !thePatientId.isEmpty()) {
			Optional<Kontakt> patient = KontaktService.INSTANCE.findById(thePatientId.getIdPart());
			if (patient.isPresent()) {
				if (patient.get().isPatient()) {
					List<IFinding> findings = findingsService.getPatientsFindings(thePatientId.getIdPart(),
							IProcedureRequest.class);
					if(theEncounterId != null) {
						Optional<IFinding> encounter = findingsService.findById(theEncounterId.getIdPart(), IEncounter.class);
						if(encounter.isPresent()) {
							IEncounter iEncounter = (IEncounter) encounter.get();
							String consid = iEncounter.getConsultationId();
							if (consid != null && !consid.isEmpty()) {
								findings = findingsService.getConsultationsFindings(consid, IProcedureRequest.class);
							}
						}
					}
					if (findings != null && !findings.isEmpty()) {
						List<ProcedureRequest> ret = new ArrayList<ProcedureRequest>();
						for (IFinding iFinding : findings) {
							Optional<ProcedureRequest> fhirProcedureRequest = getTransformer()
									.getFhirObject((IProcedureRequest) iFinding);
							fhirProcedureRequest.ifPresent(pr -> ret.add(pr));
						}
						return ret;
					}
				}
			}
		}
		return Collections.emptyList();
	}

	@Create
	public MethodOutcome createProcedureRequest(@ResourceParam ProcedureRequest procedureRequest) {
		MethodOutcome outcome = new MethodOutcome();
		Optional<IProcedureRequest> exists = getTransformer().getLocalObject(procedureRequest);
		if (exists.isPresent()) {
			outcome.setCreated(false);
			outcome.setId(new IdType(procedureRequest.getId()));
		} else {
			Optional<IProcedureRequest> created = getTransformer().createLocalObject(procedureRequest);
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
