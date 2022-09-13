package es.fhir.rest.core.resources;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.IdType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ch.elexis.core.findings.IDocumentReference;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.migration.IMigratorService;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.IPatient;
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
}
