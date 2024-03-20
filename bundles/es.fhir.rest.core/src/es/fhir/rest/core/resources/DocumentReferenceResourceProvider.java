package es.fhir.rest.core.resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Person;
import org.hl7.fhir.r4.model.Practitioner;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.PreconditionFailedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import ch.elexis.core.findings.IDocumentReference;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.migration.IMigratorService;
import ch.elexis.core.findings.util.CodeTypeUtil;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.BriefConstants;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.IDocumentTemplate;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IContext;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IDocumentService;
import ch.elexis.core.services.IDocumentStore;
import ch.elexis.core.services.ILocalLockService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IStoreToStringService;
import ch.elexis.core.services.ITextReplacementService;
import ch.elexis.core.status.ObjectStatus;

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
	private IContextService contextService;

	@Reference
	private IStoreToStringService storeToStringService;

	@Reference
	private IDocumentService documentService;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private List<IDocumentStore> documentStores;

	@Reference
	private Gson gson;

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
		return transformerRegistry.getTransformerFor(DocumentReference.class, IDocumentReference.class);
	}

	private IFhirTransformer<DocumentReference, IDocument> getDocumentTransformer() {
		return transformerRegistry.getTransformerFor(DocumentReference.class, IDocument.class);
	}

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return DocumentReference.class;
	}

	@Search
	public List<DocumentReference> search(@OptionalParam(name = DocumentReference.SP_PATIENT) IdType thePatientId,
			@OptionalParam(name = DocumentReference.SP_SUBJECT) IdType theSubjectId,
			@OptionalParam(name = DocumentReference.SP_CATEGORY) CodeType categoryCode) {

		if (thePatientId == null && theSubjectId != null) {
			thePatientId = theSubjectId;
		}

		List<DocumentReference> ret = new ArrayList<>();
		if (thePatientId != null && !thePatientId.isEmpty()) {
			Optional<IPatient> patient = coreModelService.load(thePatientId.getIdPart(), IPatient.class);
			if (patient.isPresent()) {
				// migrate documents first
				migratorService.migratePatientsFindings(thePatientId.getIdPart(), IDocumentReference.class, null);

				List<IDocumentReference> findings = findingsService.getPatientsFindings(patient.get().getId(),
						IDocumentReference.class);
				if (findings != null && !findings.isEmpty()) {
					findings = findings.stream().filter(f -> f.getDocument() != null).collect(Collectors.toList());
					ret.addAll(findings.stream().map(iFinding -> getTransformer().getFhirObject(iFinding).get())
							.collect(Collectors.toList()));
				}
			}
		}
		if (categoryCode != null) {
			// add templates if no patient and category present
			if ((thePatientId == null || thePatientId.isEmpty())
					&& BriefConstants.TEMPLATE.equalsIgnoreCase(CodeTypeUtil.getCode(categoryCode).orElse(""))) {
				List<DocumentReference> allTemplates = documentStores.stream()
						.flatMap(ds -> ds.getDocumentTemplates(true).stream())
						.map(dt -> getDocumentTransformer().getFhirObject(dt).get()).collect(Collectors.toList());
				ret.addAll(allTemplates);
			}
			// filter category
			ret = ret.stream()
					.filter(dr -> CodeTypeUtil.isCodeInConceptList(dr.getCategory(),
							CodeTypeUtil.getSystem(categoryCode).orElse(null),
							CodeTypeUtil.getCode(categoryCode).orElse(null)))
					.collect(Collectors.toList());
		}
		return ret;
	}

	@Delete
	@Override
	public void delete(@IdParam IdType theId) {
		IDocument document = loadLocalDocument(theId.getIdPart());
		if (document != null) {
			IDocumentStore store = documentStores.stream().filter(s -> s.getId().equals(document.getStoreId()))
					.findFirst().get();
			store.removeDocument(document);
		}
		super.delete(theId);
	}

	/**
	 * $binary-access-read
	 */
	@Operation(name = "$binary-access-read", manualResponse = true, idempotent = true)
	public void binaryAccessRead(@IdParam IIdType theResourceId, ServletRequestDetails theRequestDetails,
			HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws IOException {
		String idPart = theResourceId.getIdPart();
		IDocument document = loadLocalDocument(idPart);
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
				LoggerFactory.getLogger(DocumentReferenceResourceProvider.class).error("Error reading document content",
						e);
			}
		} else {
			throw new ResourceNotFoundException(theResourceId);
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
			IDocument document = loadLocalDocument(idPart);
			Optional<IDocumentReference> ref = findingsService.findById(idPart, IDocumentReference.class);
			if (document != null) {
				try {
					IDocumentStore store = documentStores.stream().filter(s -> s.getId().equals(document.getStoreId()))
							.findFirst().get();

					String requestContentType = theServletRequest.getContentType();
					if (StringUtils.isBlank(requestContentType)) {
						throw new InvalidRequestException("No content-target supplied");
					}
					store.saveDocument(document, new ByteArrayInputStream(theRequestDetails.loadRequestContents()));

					if (ref.isPresent()) {
						DocumentReference resource = getTransformer().getFhirObject(ref.get()).orElse(null);
						outcome.setResource(resource).setId(resource.getIdElement());
					} else {
						DocumentReference resource = getDocumentTransformer().getFhirObject(document).orElse(null);
						outcome.setResource(resource).setId(resource.getIdElement());
					}
				} catch (Exception e) {
					LoggerFactory.getLogger(getClass())
							.error("Error setting binary content of document [" + document + "]", e);
				}
			}
		}
		return outcome.getResource();
	}

	@Operation(name = "$validatetemplate", idempotent = false)
	public Parameters validateTemplate(@IdParam IIdType theTemplateId,
			@OperationParam(name = "context") CodeableConcept theContext) {
		Parameters ret = null;
		String idPart = theTemplateId.getIdPart();
		if (idPart != null) {
			IDocument document = loadLocalDocument(idPart);

			if (document instanceof IDocumentLetter && ((IDocumentLetter) document).isTemplate()) {
				IDocumentTemplate template = coreModelService.load(document.getId(), IDocumentTemplate.class).get();
				Map<String, Boolean> validationResult = documentService.validateTemplate(template,
						toContext(theContext));
				ret = toFhirValidationResult(validationResult);
			} else {
				throw new PreconditionFailedException("Document is not available or not a document template");
			}
		}
		return ret;
	}

	private Parameters toFhirValidationResult(Map<String, Boolean> validationResult) {
		Parameters ret = new Parameters();
		HashSet<String> typeNotResolvedSet = new HashSet<String>();
		// add all not resolve
		for (String placeholder : validationResult.keySet()) {
			String placeholderType = ITextReplacementService.getPlaceholderType(placeholder);
			if (!validationResult.get(placeholder)) {
				typeNotResolvedSet.add(placeholderType);
			}
		}
		// remove if resolved once resolved
		for (String placeholder : validationResult.keySet()) {
			String placeholderType = ITextReplacementService.getPlaceholderType(placeholder);
			if (validationResult.get(placeholder)) {
				typeNotResolvedSet.remove(placeholderType);
			}
		}
		typeNotResolvedSet.forEach(v -> {
			ret.addParameter(v, getFhirTypes(v));
		});
		return ret;
	}

	private String getFhirTypes(String placeholderType) {
		switch (placeholderType) {
		case "Patient":
			return Patient.class.getSimpleName();
		case "Mandant":
			return Practitioner.class.getSimpleName();
		case "Adressat":
			return Patient.class.getSimpleName() + "," + Organization.class.getSimpleName() + ","
					+ Person.class.getSimpleName() + "," + Practitioner.class.getSimpleName();
		}

		return "unknown";
	}

	@Operation(name = "$createdocument", idempotent = false)
	public DocumentReference createFromTemplate(@IdParam IIdType theTemplateId,
			@OperationParam(name = "context") CodeableConcept theContext) {
		DocumentReference createdResource = null;
		String idPart = theTemplateId.getIdPart();
		if (idPart != null) {
			if (contextContains(theContext, IPatient.class)) {
				IDocument document = loadLocalDocument(idPart);

				if (document instanceof IDocumentLetter && ((IDocumentLetter) document).isTemplate()) {
					IDocumentTemplate template = coreModelService.load(document.getId(), IDocumentTemplate.class).get();
					ObjectStatus<IDocument> createdDocument = documentService.createDocument(template,
							toContext(theContext));
					if (!createdDocument.isOK()) {
						throw new PreconditionFailedException(createdDocument.getMessage());
					}
					IDocumentReference createdDocumentReference = findingsService.create(IDocumentReference.class);
					createdDocumentReference.setDocument(createdDocument.get());
					findingsService.saveFinding(createdDocumentReference);

					createdResource = getTransformer().getFhirObject(createdDocumentReference).orElse(null);
				} else {
					throw new PreconditionFailedException("Document is not available or not a document template");
				}
			} else {
				throw new PreconditionFailedException("Can not create document without patient in context");
			}
		}
		return createdResource;
	}

	/**
	 * Try to load by document reference or direct by document store.
	 * 
	 * @param idPart
	 * @return
	 */
	private IDocument loadLocalDocument(String idPart) {
		IDocument ret = null;
		Optional<IDocumentReference> templateReference = findingsService.findById(idPart, IDocumentReference.class);
		if (templateReference.isPresent()) {
			ret = templateReference.get().getDocument();
		} else {
			for (IDocumentStore iDocumentStore : documentStores) {
				ret = iDocumentStore.loadDocument(idPart).orElse(null);
				if (ret != null) {
					break;
				}
			}
		}
		return ret;
	}

	private IContext toContext(CodeableConcept theContext) {
		IContext ret = contextService.createNamedContext("create_document_context");
		if (theContext != null) {
			for (Coding coding : theContext.getCoding()) {
				if (StringUtils.isNotBlank(coding.getCode())) {
					if (coding.getCode().indexOf("/") > -1) {
						String[] parts = coding.getCode().split("/");
						if (parts.length == 2) {
							Optional<? extends Identifiable> localObject = transformerRegistry
									.getLocalObjectForReference(coding.getCode());
							if (localObject.isPresent()) {
								if (coding.getSystem().startsWith("typed")) {
									ret.setTyped(localObject.get());
								} else {
									ret.setNamed(coding.getSystem(), localObject.get());
									if (localObject.get() instanceof IPatient) {
										ret.setTyped(localObject.get());
									}
								}
							} else {
								LoggerFactory.getLogger(getClass())
										.warn("No local object for FHIR Reference [" + coding.getCode() + "]");
							}
						} else {
							LoggerFactory.getLogger(getClass())
									.warn("Unknown FHIR Reference format [" + coding.getCode() + "]");
						}
					} else {
						Optional<Identifiable> identifiable = storeToStringService.loadFromString(coding.getCode());
						if (identifiable.isPresent()) {
							if (coding.getSystem().startsWith("typed")) {
								ret.setTyped(identifiable.get());
							} else {
								ret.setNamed(coding.getSystem(), identifiable.get());
							}
						}
					}
				}
			}
		}
		return ret;
	}

	private boolean contextContains(CodeableConcept theContext, Class<?> clazz) {
		for (Coding coding : theContext.getCoding()) {
			if (StringUtils.isNotBlank(coding.getCode())) {
				if (coding.getCode().indexOf("/") > -1) {
					String[] parts = coding.getCode().split("/");
					if (parts.length == 2) {
						Optional<? extends Identifiable> localObject = transformerRegistry
								.getLocalObjectForReference(coding.getCode());
						if (localObject.isPresent() && clazz.isAssignableFrom(localObject.get().getClass())) {
							return true;
						}
					} else {
						LoggerFactory.getLogger(getClass())
								.warn("Unknown FHIR Reference format [" + coding.getCode() + "]");
					}
				} else {
					Optional<Identifiable> identifiable = storeToStringService.loadFromString(coding.getCode());
					if (identifiable.isPresent()) {
						if (identifiable.isPresent() && clazz.isAssignableFrom(identifiable.get().getClass())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
