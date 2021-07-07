package es.fhir.rest.core.resources;

import java.util.Optional;

import org.apache.shiro.SecurityUtils;
import org.hl7.fhir.dstu3.model.BaseResource;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.slf4j.Logger;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import ch.elexis.core.model.Identifiable;
import es.fhir.rest.core.IFhirTransformer;

public class ResourceProviderUtil {
	
	protected <T extends BaseResource, U extends Identifiable> MethodOutcome updateResource(
		IdType theId, IFhirTransformer<T, U> transformer, T fhirObject, Logger log){
		
		String versionId = theId.getVersionIdPart();
		
		Optional<U> localObject = transformer.getLocalObject(fhirObject);
		MethodOutcome outcome = new MethodOutcome();
		if (localObject.isPresent()) {
			if (versionId == null) {
				log.warn("[{}] Version agnostic update on {}",
					SecurityUtils.getSubject().getPrincipal(), localObject.get());
			}
			if (versionId != null
				&& !versionId.equals(localObject.get().getLastupdate().toString())) {
				throw new ResourceVersionConflictException(
					"Expected version " + localObject.get().getLastupdate().toString());
			}
			transformer.updateLocalObject(fhirObject, localObject.get());
			
			Optional<T> updatedObject = transformer.getFhirObject(localObject.get());
			if (updatedObject.isPresent()) {
				outcome.setId(updatedObject.get().getIdElement());
				outcome.setResource(updatedObject.get());
				return outcome;
			}
			log.warn("Object update failed [{}]", fhirObject);
			throw new InternalErrorException("Object update failed");
			
		} else {
			OperationOutcome issueOutcome = new OperationOutcome();
			issueOutcome.addIssue().setDiagnostics("No local object found");
			outcome.setOperationOutcome(issueOutcome);
		}
		return outcome;
	}
	
	/**
	 * 
	 * @param transformer
	 * @param fhirObject
	 * @param log
	 * @return TODO Support conditional create http://hl7.org/fhir/http.html#ccreate
	 */
	protected <T extends BaseResource, U extends Identifiable> MethodOutcome createResource(
		IFhirTransformer<T, U> transformer, T fhirObject, Logger log){
		
		Optional<U> created = transformer.createLocalObject(fhirObject);
		if (created.isPresent()) {
			Optional<T> updated = transformer.getFhirObject(created.get());
			if (updated.isPresent()) {
				MethodOutcome outcome = new MethodOutcome();
				outcome.setCreated(true);
				outcome.setId(updated.get().getIdElement());
				outcome.setResource(updated.get());
				return outcome;
			}
		}
		
		log.warn("Object creation failed [{}]", fhirObject);
		throw new InternalErrorException("Creation failed");
	}
	
}
