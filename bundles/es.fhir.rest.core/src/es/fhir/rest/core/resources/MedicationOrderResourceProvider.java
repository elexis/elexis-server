package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationOrder;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription_;
import info.elexis.server.core.connector.elexis.services.JPAQuery;
import info.elexis.server.core.connector.elexis.services.KontaktService;
import info.elexis.server.core.connector.elexis.services.PrescriptionService;

@Component
public class MedicationOrderResourceProvider implements IFhirResourceProvider {

	private IFhirTransformer<MedicationOrder, Prescription> prescriptionMapper;

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return MedicationOrder.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initTransformer(IFhirTransformerRegistry transformerRegistry) {
		prescriptionMapper = (IFhirTransformer<MedicationOrder, Prescription>) transformerRegistry
				.getTransformerFor(MedicationOrder.class, Prescription.class);
	}

	@Read
	public MedicationOrder getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<Prescription> prescription = PrescriptionService.INSTANCE.findById(idPart);
			if (prescription.isPresent()) {
				Optional<MedicationOrder> fhirMedicationOrder = prescriptionMapper.getFhirObject(prescription.get());
				return fhirMedicationOrder.get();
			}
		}
		return null;
	}

	@Search()
	public List<MedicationOrder> findMedicationsByPatient(
			@RequiredParam(name = MedicationOrder.SP_PATIENT) String thePatientId) {
		if (thePatientId != null && !thePatientId.isEmpty()) {
			Optional<Kontakt> patient = KontaktService.INSTANCE.findById(thePatientId);
			if (patient.isPresent()) {
				if (patient.get().isPatient()) {
					JPAQuery<Prescription> qbe = new JPAQuery<Prescription>(Prescription.class);
					qbe.add(Prescription_.patient, JPAQuery.QUERY.EQUALS, patient.get());
					qbe.add(Prescription_.rezeptID, JPAQuery.QUERY.EQUALS, null);
					List<Prescription> prescriptions = qbe.execute();
					if (prescriptions != null && !prescriptions.isEmpty()) {
						List<MedicationOrder> ret = new ArrayList<MedicationOrder>();
						for (Prescription prescription : prescriptions) {
							Optional<MedicationOrder> fhirMedicationOrder = prescriptionMapper
									.getFhirObject(prescription);
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
	public MethodOutcome updateMedicationOrder(@ResourceParam MedicationOrder updateOrder) {
		Optional<Prescription> localObject = prescriptionMapper.getLocalObject(updateOrder);
		MethodOutcome outcome = new MethodOutcome();
		if (localObject.isPresent()) {
			try {
				prescriptionMapper.updateLocalObject(updateOrder, localObject.get());

				outcome.setId(updateOrder.getIdElement());
				outcome.setResource(updateOrder);
			} catch (RuntimeException e) {
				OperationOutcome issueOutcome = new OperationOutcome();
				issueOutcome.addIssue().setDiagnostics("Update failed. " + e.getMessage());
			}
		} else {
			OperationOutcome issueOutcome = new OperationOutcome();
			issueOutcome.addIssue().setDiagnostics("No local object found");
		}
		return outcome;
	}
}
