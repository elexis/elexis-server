package es.fhir.rest.core.resources;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;

public abstract class AbstractFhirCrudResourceProvider<FHIR extends BaseResource, ELEXIS extends Identifiable & Deleteable>
		implements IFhirResourceProvider<FHIR, ELEXIS> {
	
	private final Class<ELEXIS> CLAZZ;
	private IModelService coreModelService;
	
	protected Logger log;
	protected ResourceProviderUtil resourceProviderUtil;
	
	public AbstractFhirCrudResourceProvider(Class<ELEXIS> clazz){
		this.CLAZZ = clazz;
		resourceProviderUtil = new ResourceProviderUtil();
		log = LoggerFactory.getLogger(getClass());
	}
	
	protected void setCoreModelService(IModelService coreModelService){
		this.coreModelService = coreModelService;
	}
	
	@Create
	public MethodOutcome create(@ResourceParam FHIR patient){
		return resourceProviderUtil.createResource(getTransformer(), patient, log);
	}
	
	@Read
	public FHIR read(@IdParam IdType theId){
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<ELEXIS> elexisObjOptional = coreModelService.load(idPart, CLAZZ);
			if (elexisObjOptional.isPresent()) {
				Optional<FHIR> elexisObj = getTransformer().getFhirObject(elexisObjOptional.get());
				return elexisObj.get();
				
			}
		}
		return null;
	}
	
	@Update
	public MethodOutcome update(@IdParam IdType theId, @ResourceParam FHIR patient){
		// FIXME request lock or fail
		return resourceProviderUtil.updateResource(theId, getTransformer(), patient, log);
	}
	
	@Delete
	public void delete(@IdParam IdType theId){
		// TODO request lock or fail
		if (theId != null) {
			Optional<ELEXIS> resource = coreModelService.load(theId.getIdPart(), CLAZZ);
			if (!resource.isPresent()) {
				throw new ResourceNotFoundException(theId);
			}
			coreModelService.delete(resource.get());
		}
	}

	public List<FHIR> handleExecute(IQuery<ELEXIS> query){
		// TODO add limit?
		
		List<ELEXIS> objects = query.execute();
		if (objects.isEmpty()) {
			return Collections.emptyList();
		}
		List<FHIR> _objects =
			objects.parallelStream().map(org -> getTransformer().getFhirObject(org))
				.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
		return _objects;
	}
	
}
