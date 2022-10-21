package es.fhir.rest.core.resources;

import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.slf4j.Logger;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.PreconditionFailedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerException;
import ch.elexis.core.model.Identifiable;

public class ResourceProviderUtil {

	protected <T extends BaseResource, U extends Identifiable> MethodOutcome updateResource(IdType theId,
			IFhirTransformer<T, U> transformer, T fhirObject, Logger log) {

		String versionId = theId.getVersionIdPart();

		Optional<U> localObject = transformer.getLocalObject(fhirObject);
		MethodOutcome outcome = new MethodOutcome();
		if (localObject.isPresent()) {
			if (versionId == null) {
				log.warn("Version agnostic update on {}", localObject.get());
			}
			if (versionId != null && !versionId.equals(localObject.get().getLastupdate().toString())) {
				throw new ResourceVersionConflictException(
						"Expected version " + localObject.get().getLastupdate().toString());
			}
			try {
				transformer.updateLocalObject(fhirObject, localObject.get());
			} catch (IFhirTransformerException e) {
				OperationOutcome opOutcome = generateOperationOutcome(e);
				throw new PreconditionFailedException(e.getMessage(), opOutcome);
			}

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
			IFhirTransformer<T, U> transformer, T fhirObject, Logger log) {

		Optional<U> created;
		try {
			created = transformer.createLocalObject(fhirObject);
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
		} catch (IFhirTransformerException e) {
			OperationOutcome opOutcome = generateOperationOutcome(e);
			throw new PreconditionFailedException(e.getMessage(), opOutcome);
		}

		log.warn("Object creation failed [{}]", fhirObject);
		throw new InternalErrorException("Creation failed");
	}

	public OperationOutcome generateOperationOutcome(IFhirTransformerException e) {
		OperationOutcome opOutcome = new OperationOutcome();
		OperationOutcomeIssueComponent ooc = new OperationOutcomeIssueComponent();
		OperationOutcome.IssueSeverity severity = OperationOutcome.IssueSeverity.valueOf(e.getSeverity().toUpperCase());
		ooc.setSeverity(severity);
		IssueType issueType = e.getCode() == 412 ? IssueType.PROCESSING : IssueType.BUSINESSRULE;
		ooc.setCode(issueType);
		ooc.setDiagnostics(e.getMessage());
		opOutcome.addIssue(ooc);
		return opOutcome;
	}

	/**
	 * Convert an {@link IStatus} into an OperationOutcome
	 * 
	 * @param status
	 * @return
	 * @since 3.10
	 */
	public static OperationOutcome statusToOperationOutcome(IStatus status) {
		OperationOutcome opOutcome = new OperationOutcome();
		OperationOutcomeIssueComponent opOutcomeComponent = new OperationOutcomeIssueComponent();
		opOutcome.addIssue(opOutcomeComponent);
		if (status.isOK()) {
			opOutcomeComponent.setSeverity(IssueSeverity.INFORMATION);
			opOutcomeComponent.setCode(IssueType.INFORMATIONAL);
		} else {
			switch (status.getSeverity()) {
			case IStatus.ERROR:
				opOutcomeComponent.setSeverity(IssueSeverity.ERROR);
				break;
			case IStatus.WARNING:
				opOutcomeComponent.setSeverity(IssueSeverity.WARNING);
				break;
			default:
				opOutcomeComponent.setSeverity(IssueSeverity.INFORMATION);
				break;
			}
			opOutcomeComponent.setCode(IssueType.BUSINESSRULE);
			opOutcomeComponent.setDiagnostics(status.getMessage());
		}

		return opOutcome;
	}

}
