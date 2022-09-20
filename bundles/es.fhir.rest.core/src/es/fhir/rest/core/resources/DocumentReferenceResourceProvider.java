package es.fhir.rest.core.resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.IdType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import ch.elexis.core.findings.IDocumentReference;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.migration.IMigratorService;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IDocumentStore;
import ch.elexis.core.services.ILocalLockService;
import ch.elexis.core.services.IModelService;

@Component(service = IFhirResourceProvider.class)
public class DocumentReferenceResourceProvider
		extends AbstractFhirCrudResourceProvider<DocumentReference, IDocumentReference> {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.findings.model)")
	private IModelService findingsModelService;

	@Reference
	private ILocalLockService localLockService;

	@Reference
	private IFhirTransformerRegistry transformerRegistry;

	@Reference
	private IMigratorService migratorService;

	@Reference
	private IFindingsService findingsService;

	@Reference
	private List<IDocumentStore> documentStores;

	public DocumentReferenceResourceProvider() {
		super(IDocumentReference.class);
	}

	@Activate
	public void activate() {
		super.setModelService(findingsModelService);
		super.setLocalLockService(localLockService);
	}

	@Override
	public IFhirTransformer<DocumentReference, IDocumentReference> getTransformer() {
		return (IFhirTransformer<DocumentReference, IDocumentReference>) transformerRegistry
				.getTransformerFor(DocumentReference.class, IDocumentReference.class);
	}

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return DocumentReference.class;
	}

	@Search
	public List<DocumentReference> searchPatient(
			@OptionalParam(name = DocumentReference.SP_PATIENT) IdType thePatientId,
			@OptionalParam(name = DocumentReference.SP_SUBJECT) IdType theSubjectId) {

		if (thePatientId == null && theSubjectId != null) {
			thePatientId = theSubjectId;
		}

		if (thePatientId != null && !thePatientId.isEmpty()) {
			Optional<IPatient> patient = coreModelService.load(thePatientId.getIdPart(), IPatient.class);
			if (patient.isPresent()) {
				// migrate documents first
				migratorService.migratePatientsFindings(thePatientId.getIdPart(), IDocumentReference.class, null);

				List<IDocumentReference> findings = findingsService.getPatientsFindings(patient.get().getId(),
						IDocumentReference.class);
				if (findings != null && !findings.isEmpty()) {
					return findings.stream().map(iFinding -> getTransformer().getFhirObject(iFinding).get())
							.collect(Collectors.toList());
				}
			}
		}

		return null;
	}

	/**
	 * $binary-access-read
	 */
	@Operation(name = "$binary-access-read", manualResponse = true, idempotent = true)
	public void binaryAccessRead(@IdParam IIdType theResourceId, ServletRequestDetails theRequestDetails,
			HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws IOException {
		String idPart = theResourceId.getIdPart();
		if (idPart != null) {
			Optional<IDocumentReference> documentReference = findingsService.findById(idPart, IDocumentReference.class);
			documentReference.ifPresent(ref -> {
				IDocument document = ref.getDocument();
				if (document != null) {
					try {
						String contentType = document.getMimeType();
						contentType = StringUtils.defaultIfBlank(contentType, "application/octet-stream");

						theServletResponse.setStatus(200);
						theServletResponse.setContentType(contentType);
						theServletResponse.setContentLength((int) document.getContentLength());

						RestfulServer server = theRequestDetails.getServer();
						server.addHeadersToResponse(theServletResponse);

						IOUtils.copy(document.getContent(), theServletResponse.getOutputStream());
						theServletResponse.getOutputStream().close();
					} catch (Exception e) {
						LoggerFactory.getLogger(DocumentReferenceResourceProvider.class)
								.error("Error reading document content", e);
					}
				}
			});
		}
	}

	/**
	 * $binary-access-write
	 */
	@Operation(name = "$binary-access-write", manualRequest = true, idempotent = false)
	public IBaseResource binaryAccessWrite(@IdParam IIdType theResourceId, ServletRequestDetails theRequestDetails,
			HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws IOException {

		MethodOutcome outcome = new MethodOutcome();
		String idPart = theResourceId.getIdPart();
		if (idPart != null) {
			Optional<IDocumentReference> documentReference = findingsService.findById(idPart, IDocumentReference.class);
			documentReference.ifPresent(ref -> {
				IDocument document = ref.getDocument();
				if (document != null) {
					try {
						IDocumentStore store = documentStores.stream()
								.filter(s -> s.getId().equals(document.getStoreId())).findFirst().get();

						String requestContentType = theServletRequest.getContentType();
						if (StringUtils.isBlank(requestContentType)) {
							throw new InvalidRequestException("No content-target supplied");
						}
						store.saveDocument(document, new ByteArrayInputStream(theRequestDetails.loadRequestContents()));

						DocumentReference resource = getTransformer().getFhirObject(ref).orElse(null);
						outcome.setResource(resource).setId(resource.getIdElement());
					} catch (Exception e) {
						LoggerFactory.getLogger(getClass())
								.error("Error setting binary content of document [" + document + "]", e);
					}
				}
			});
		}
		return outcome.getResource();
	}
}
