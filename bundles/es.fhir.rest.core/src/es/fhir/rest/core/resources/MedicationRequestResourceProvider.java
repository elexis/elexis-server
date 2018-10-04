package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;

@Component
public class MedicationRequestResourceProvider implements IFhirResourceProvider {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;
	
	@Reference
	private IFhirTransformerRegistry transformerRegistry;
	
	@Override
	public Class<? extends IBaseResource> getResourceType(){
		return MedicationRequest.class;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<MedicationRequest, IPrescription> getTransformer(){
		return (IFhirTransformer<MedicationRequest, IPrescription>) transformerRegistry
			.getTransformerFor(MedicationRequest.class, IPrescription.class);
	}
	
	@Read
	public MedicationRequest getResourceById(@IdParam IdType theId){
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<IPrescription> prescription = modelService.load(idPart, IPrescription.class);
			if (prescription.isPresent()) {
				Optional<MedicationRequest> fhirMedicationOrder =
					getTransformer().getFhirObject(prescription.get());
				return fhirMedicationOrder.get();
			}
		}
		return null;
	}
	
	@Search()
	public List<MedicationRequest> findMedicationsByPatient(
		@RequiredParam(name = MedicationRequest.SP_PATIENT) IdType thePatientId){
		if (thePatientId != null && !thePatientId.isEmpty()) {
			Optional<IPatient> patient =
				modelService.load(thePatientId.getIdPart(), IPatient.class);
			if (patient.isPresent()) {
				if (patient.get().isPatient()) {
					IQuery<IPrescription> query = modelService.getQuery(IPrescription.class);
					// TODO
					//					JPAQuery<Prescription> qbe = new JPAQuery<Prescription>(Prescription.class);
					//					qbe.add(Prescription_.patient, JPAQuery.QUERY.EQUALS, patient.get());
					//					qbe.add(Prescription_.rezeptID, JPAQuery.QUERY.EQUALS, null);
					List<IPrescription> prescriptions = query.execute();
					if (prescriptions != null && !prescriptions.isEmpty()) {
						List<MedicationRequest> ret = new ArrayList<MedicationRequest>();
						for (IPrescription prescription : prescriptions) {
							Optional<MedicationRequest> fhirMedicationOrder =
								getTransformer().getFhirObject(prescription);
							fhirMedicationOrder.ifPresent(fmo -> ret.add(fmo));
						}
						return ret;
					}
				}
			}
		}
		return null;
	}
	
	@Update
	public MethodOutcome updateMedicationOrder(@ResourceParam MedicationRequest updateOrder){
		Optional<IPrescription> localObject = getTransformer().getLocalObject(updateOrder);
		MethodOutcome outcome = new MethodOutcome();
		outcome.setCreated(false);
		if (localObject.isPresent()) {
			try {
				Optional<IPrescription> updated =
					getTransformer().updateLocalObject(updateOrder, localObject.get());
				updated.ifPresent(prescription -> {
					outcome.setCreated(true);
					outcome.setId(new IdType(prescription.getId()));
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
