package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IVaccination;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;

@Component
public class ImmunizationResourceProvider implements IFhirResourceProvider<Immunization, IVaccination> {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;
	
	@Reference
	private IFhirTransformerRegistry transformerRegistry;
	
	@Override
	public Class<? extends IBaseResource> getResourceType(){
		return Immunization.class;
	}
	
	@Override
	public IFhirTransformer<Immunization, IVaccination> getTransformer() {
		return transformerRegistry.getTransformerFor(Immunization.class, IVaccination.class);
	}
	
	@Read
	public Immunization getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<IVaccination> prescription = modelService.load(idPart, IVaccination.class);
			if (prescription.isPresent()) {
				Optional<Immunization> fhirImmunization = getTransformer().getFhirObject(prescription.get());
				return fhirImmunization.get();
			}
		}
		return null;
	}
	
	@Search()
	public List<Immunization> findImmunizationsByPatient(
			@RequiredParam(name = Immunization.SP_PATIENT) IdType thePatientId) {
		if (thePatientId != null && !thePatientId.isEmpty()) {
			Optional<IPatient> patient =
				modelService.load(thePatientId.getIdPart(), IPatient.class);
			if (patient.isPresent()) {
				if (patient.get().isPatient()) {
					IQuery<IVaccination> query = modelService.getQuery(IVaccination.class);
					query.and(ModelPackage.Literals.IPRESCRIPTION__PATIENT, COMPARATOR.EQUALS,
						patient.get());
					List<IVaccination> vaccinations = query.execute();
					if (vaccinations != null && !vaccinations.isEmpty()) {
						List<Immunization> ret = new ArrayList<>();
						for (IVaccination vaccination : vaccinations) {
							Optional<Immunization> fhirImmunization =
								getTransformer().getFhirObject(vaccination);
							fhirImmunization.ifPresent(fmo -> ret.add(fmo));
						}
						return ret;
					}
				}
			}
		}
		return null;
	}
	
	@Create
	public MethodOutcome create(@ResourceParam Immunization fhirObject) {
		MethodOutcome outcome = new MethodOutcome();
		Optional<IVaccination> exists = getTransformer().getLocalObject(fhirObject);
		if (exists.isPresent()) {
			outcome.setCreated(false);
			outcome.setId(new IdType(fhirObject.getId()));
		} else {
			Optional<IVaccination> created = getTransformer().createLocalObject(fhirObject);
			if (created.isPresent()) {
				Optional<Immunization> createdFhir = getTransformer().getFhirObject(created.get());
				if (createdFhir.isPresent()) {
					outcome.setCreated(true);
					outcome.setId(createdFhir.get().getIdElement());
					outcome.setResource(createdFhir.get());
				}
			} else {
				throw new InvalidRequestException("Could not create local object");
			}
		}
		return outcome;
	}

	@Update
	public MethodOutcome updateMedicationOrder(@ResourceParam Immunization updateOrder) {
		Optional<IVaccination> localObject = getTransformer().getLocalObject(updateOrder);
		MethodOutcome outcome = new MethodOutcome();
		outcome.setCreated(false);
		if (localObject.isPresent()) {
			try {
				Optional<IVaccination> updated = getTransformer().updateLocalObject(updateOrder, localObject.get());
				updated.ifPresent(vaccination -> {
					if (vaccination.getId().equals(localObject.get().getId())) {
						outcome.setId(new IdType(vaccination.getId()));
					} else {
						outcome.setCreated(true);
						outcome.setId(new IdType(vaccination.getId()));
					}
				});
			} catch (RuntimeException e) {
				OperationOutcome issueOutcome = new OperationOutcome();
				issueOutcome.addIssue().setDiagnostics("Update failed. " + e.getMessage());
				outcome.setOperationOutcome(issueOutcome);
			}
		} else {
			OperationOutcome issueOutcome = new OperationOutcome();
			issueOutcome.addIssue().setDiagnostics("No local object found");
			outcome.setOperationOutcome(issueOutcome);
		}
		return outcome;
	}
}
