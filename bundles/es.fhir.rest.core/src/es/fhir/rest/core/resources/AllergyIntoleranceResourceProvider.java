package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.IdType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ch.elexis.core.findings.IAllergyIntolerance;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IModelService;

@Component
public class AllergyIntoleranceResourceProvider implements IFhirResourceProvider {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;
	
	@Reference
	private IFhirTransformerRegistry transformerRegistry;
	
	@Reference
	private IFindingsService findingsService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType(){
		return AllergyIntolerance.class;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<AllergyIntolerance, IAllergyIntolerance> getTransformer(){
		return (IFhirTransformer<AllergyIntolerance, IAllergyIntolerance>) transformerRegistry
			.getTransformerFor(AllergyIntolerance.class, IAllergyIntolerance.class);
	}
	
	@Search()
	public List<AllergyIntolerance> findAllergyIntolerance(
		@RequiredParam(name = AllergyIntolerance.SP_PATIENT)
		IdType patientId){
		if (patientId != null && !patientId.isEmpty()) {
			Optional<IPatient> patient = modelService.load(patientId.getIdPart(), IPatient.class);
			if (patient.isPresent()) {
				if (patient.get().isPatient()) {
					List<AllergyIntolerance> ret = new ArrayList<>();
					List<IAllergyIntolerance> findings = findingsService
						.getPatientsFindings(patientId.getIdPart(), IAllergyIntolerance.class);
					if (findings != null && !findings.isEmpty()) {
						for (IAllergyIntolerance iFinding : findings) {
							Optional<AllergyIntolerance> fhirAllergyIntolerance =
								getTransformer().getFhirObject(iFinding);
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
	public MethodOutcome createAllergyIntolerance(@ResourceParam
	AllergyIntolerance allergyIntolerance){
		MethodOutcome outcome = new MethodOutcome();
		
		Optional<IAllergyIntolerance> exists = getTransformer().getLocalObject(allergyIntolerance);
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
	public AllergyIntolerance getResourceById(@IdParam
	IdType theId){
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<IAllergyIntolerance> optionalAllergyIntolerance =
				findingsService.findById(idPart, IAllergyIntolerance.class);
			if (optionalAllergyIntolerance.isPresent()) {
				Optional<AllergyIntolerance> fhirAllergyIntolerance =
					getTransformer().getFhirObject(optionalAllergyIntolerance.get());
				return fhirAllergyIntolerance.get();
			}
		}
		return null;
	}
}
