package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ch.elexis.core.findings.IAllergyIntolerance;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.KontaktService;

@Component
public class AllergyIntoleranceResourceProvider implements IFhirResourceProvider {

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return AllergyIntolerance.class;
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
	public IFhirTransformer<AllergyIntolerance, IAllergyIntolerance> getTransformer(){
		return (IFhirTransformer<AllergyIntolerance, IAllergyIntolerance>) transformerRegistry
			.getTransformerFor(AllergyIntolerance.class, IAllergyIntolerance.class);
	}

	@Search()
	public List<AllergyIntolerance> findAllergyIntolerance(
		@RequiredParam(name = AllergyIntolerance.SP_PATIENT) IdType patientId){
		if (patientId != null && !patientId.isEmpty()) {
			Optional<Kontakt> patient = KontaktService.load(patientId.getIdPart());
			if (patient.isPresent()) {
				if (patient.get().isPatient()) {
					List<AllergyIntolerance> ret = new ArrayList<>();
					List<IFinding> findings = findingsService
						.getPatientsFindings(patientId.getIdPart(), IAllergyIntolerance.class);
					if (findings != null && !findings.isEmpty()) {
						for (IFinding iFinding : findings) {
							
							Optional<AllergyIntolerance> fhirAllergyIntolerance =
								getTransformer().getFhirObject((IAllergyIntolerance) iFinding);
							if (fhirAllergyIntolerance.isPresent()) {
								ret.add(fhirAllergyIntolerance.get());
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
	public MethodOutcome createAllergyIntolerance(
		@ResourceParam AllergyIntolerance allergyIntolerance){
		MethodOutcome outcome = new MethodOutcome();
		
		Optional<IAllergyIntolerance> exists =
			getTransformer().getLocalObject(allergyIntolerance);
		if (exists.isPresent()) {
			outcome.setCreated(false);
			outcome.setId(new IdType(allergyIntolerance.getId()));
		} else {
			Optional<IAllergyIntolerance> created =
				getTransformer().createLocalObject(allergyIntolerance);
			if (created.isPresent()) {
				outcome.setCreated(true);
				outcome.setId(new IdType(created.get().getId()));
			} else {
				throw new InternalErrorException("Creation failed");
			}
		}
		return outcome;
	}
	
	@Read
	public AllergyIntolerance getResourceById(@IdParam IdType theId){
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<IFinding> optionalAllergyIntolerance = findingsService.findById(idPart);
			if (optionalAllergyIntolerance.isPresent() && (optionalAllergyIntolerance.get() instanceof IAllergyIntolerance)) {
				Optional<AllergyIntolerance> fhirAllergyIntolerance =
					getTransformer().getFhirObject((IAllergyIntolerance) optionalAllergyIntolerance.get());
				return fhirAllergyIntolerance.get();
			}
		}
		return null;
	}
}
