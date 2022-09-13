package es.fhir.rest.core.resources;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ca.uhn.fhir.rest.server.exceptions.PreconditionFailedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.ILocalLockService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;

public abstract class AbstractFhirCrudResourceProvider<FHIR extends BaseResource, ELEXIS extends Identifiable & Deleteable>
		implements IFhirResourceProvider<FHIR, ELEXIS> {

	private final Class<ELEXIS> CLAZZ;
	private IModelService modelService;
	private ILocalLockService localLockService;

	protected Logger log;
	protected ResourceProviderUtil resourceProviderUtil;

	public AbstractFhirCrudResourceProvider(Class<ELEXIS> clazz) {
		this.CLAZZ = clazz;
		resourceProviderUtil = new ResourceProviderUtil();
		log = LoggerFactory.getLogger(getClass());
	}

	protected void setModelService(IModelService coreModelService) {
		this.modelService = coreModelService;
	}

	protected void setLocalLockService(ILocalLockService localLockService) {
		this.localLockService = localLockService;
	}

	@Create
	public MethodOutcome create(@ResourceParam FHIR fhirObject) {
		MethodOutcome outcome = new MethodOutcome();
		Optional<ELEXIS> exists = getTransformer().getLocalObject(fhirObject);
		if (exists.isPresent()) {
			outcome.setCreated(false);
			outcome.setId(new IdType(fhirObject.getId()));
		} else {
			outcome = resourceProviderUtil.createResource(getTransformer(), fhirObject, log);
		}
		return outcome;
	}

	@Read
	public FHIR read(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<ELEXIS> elexisObjOptional = modelService.load(idPart, CLAZZ);
			if (elexisObjOptional.isPresent()) {
				Optional<FHIR> elexisObj = getTransformer().getFhirObject(elexisObjOptional.get());
				return elexisObj.get();

			}
		}
		return null;
	}

	@Update
	public MethodOutcome update(@IdParam IdType theId, @ResourceParam FHIR fhirObject) {
		MethodOutcome outcome = new MethodOutcome();
		Optional<ELEXIS> elexisObject = getTransformer().getLocalObject(fhirObject);
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
			outcome = create(fhirObject);
		}
		return outcome;
	}

	/**
	 * Match the existing object version (i.e. lastupdate) with the version
	 * propagated by the incoming update.
	 * 
	 * @param lastupdate
	 * @param fhirObject
	 */
	private void checkMatchVersion(Long lastupdate, FHIR fhirObject) {
		if (fhirObject.getMeta() != null && fhirObject.getMeta().getLastUpdated() != null) {
			Date metaLastupdated = fhirObject.getMeta().getLastUpdated(); // this will contain the ETag
			Date lastUpdate = (lastupdate != null) ? new Date(lastupdate) : null;
			if (metaLastupdated.equals(lastUpdate)) {
				return;
			}

			throw new ResourceVersionConflictException("Expected version " + lastUpdate);
		}
	}

	@Delete
	public void delete(@IdParam IdType theId) {
		// TODO request lock or fail
		if (theId != null) {
			Optional<ELEXIS> resource = modelService.load(theId.getIdPart(), CLAZZ);
			if (!resource.isPresent()) {
				throw new ResourceNotFoundException(theId);
			}
			modelService.delete(resource.get());
		}
	}

	public List<FHIR> handleExecute(IQuery<ELEXIS> query, SummaryEnum summaryEnum, Set<Include> includes) {

		// TODO add limit?

		if (summaryEnum == SummaryEnum.COUNT) {
		}

		List<ELEXIS> objects = query.execute();
		if (objects.isEmpty()) {
			return Collections.emptyList();
		}
		// TODO WARN - parallelStream does loose the ThreadContext,
		// thus will not be able to resolve user specific data
		List<FHIR> _objects = objects.stream()
				.map(object -> getTransformer().getFhirObject(object, summaryEnum, includes))
				.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
		return _objects;
	}

}
