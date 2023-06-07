package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.IdType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.PreconditionFailedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import ch.elexis.core.findings.IAllergyIntolerance;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.ILocalLockService;
import ch.elexis.core.services.IModelService;

@Component
public class AllergyIntoleranceResourceProvider implements IFhirResourceProvider {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;
	
	@Reference
	private IFhirTransformerRegistry transformerRegistry;
	
	@Reference
	private IFindingsService findingsService;
	
	@Reference
	private ILocalLockService localLockService;

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
	public MethodOutcome create(@ResourceParam
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

	Logger log = LoggerFactory.getLogger(getClass());
	ResourceProviderUtil resourceProviderUtil = new ResourceProviderUtil();

	@Update
	public MethodOutcome update(@IdParam IdType theId, @ResourceParam AllergyIntolerance fhirObject) {
		MethodOutcome outcome = new MethodOutcome();
		Optional<IAllergyIntolerance> elexisObject = getTransformer().getLocalObject(fhirObject);
		if (elexisObject.isPresent()) {
			checkMatchVersion(elexisObject.get().getLastupdate(), fhirObject);

			LockResponse lockResponse = localLockService.acquireLock(elexisObject.get());
			if (lockResponse.isOk()) {
				outcome = resourceProviderUtil.updateResource(theId, getTransformer(), fhirObject, log);
				localLockService.releaseLock(lockResponse);
			} else {
				throw new PreconditionFailedException("Could not acquire update lock");
			}

		} else {
			log.warn("Could not find local object for [" + fhirObject + "] with id [" + fhirObject.getId() + "]");
			outcome = create(fhirObject);
		}
		return outcome;
	}

	private void checkMatchVersion(Long lastupdate, AllergyIntolerance fhirObject) {
		if (fhirObject.getMeta() != null && fhirObject.getMeta().getLastUpdated() != null) {
			Date metaLastupdated = fhirObject.getMeta().getLastUpdated(); // this will contain the ETag
			Date lastUpdate = (lastupdate != null) ? new Date(lastupdate) : null;
			if (metaLastupdated.equals(lastUpdate)) {
				return;
			}

			throw new ResourceVersionConflictException("Expected version " + lastUpdate);
		}
	}
}
