package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationOrder;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

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

@Component(immediate = true)
public class MedicationOrderResourceProvider implements IFhirResourceProvider {

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return MedicationOrder.class;
	}

	private IFhirTransformerRegistry transformerRegistry;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFhirTransformerRegistry(IFhirTransformerRegistry transformerRegistry) {
		this.transformerRegistry = transformerRegistry;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<MedicationOrder, Prescription> getTransformer() {
		return (IFhirTransformer<MedicationOrder, Prescription>) transformerRegistry
				.getTransformerFor(MedicationOrder.class, Prescription.class);
	}

	@Read
	public MedicationOrder getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<Prescription> prescription = PrescriptionService.INSTANCE.findById(idPart);
			if (prescription.isPresent()) {
				Optional<MedicationOrder> fhirMedicationOrder = getTransformer().getFhirObject(prescription.get());
				return fhirMedicationOrder.get();
			}
		}
		return null;
	}

	@Search()
	public List<MedicationOrder> findMedicationsByPatient(
			@RequiredParam(name = MedicationOrder.SP_PATIENT) IdType thePatientId) {
		if (thePatientId != null && !thePatientId.isEmpty()) {
			Optional<Kontakt> patient = KontaktService.INSTANCE.findById(thePatientId.getIdPart());
			if (patient.isPresent()) {
				if (patient.get().isPatient()) {
					JPAQuery<Prescription> qbe = new JPAQuery<Prescription>(Prescription.class);
					qbe.add(Prescription_.patient, JPAQuery.QUERY.EQUALS, patient.get());
					qbe.add(Prescription_.rezeptID, JPAQuery.QUERY.EQUALS, null);
					List<Prescription> prescriptions = qbe.execute();
					if (prescriptions != null && !prescriptions.isEmpty()) {
						List<MedicationOrder> ret = new ArrayList<MedicationOrder>();
						for (Prescription prescription : prescriptions) {
							Optional<MedicationOrder> fhirMedicationOrder = getTransformer()
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
		Optional<Prescription> localObject = getTransformer().getLocalObject(updateOrder);
		MethodOutcome outcome = new MethodOutcome();
		outcome.setCreated(false);
		if (localObject.isPresent()) {
			try {
				Optional<Prescription> updated = getTransformer().updateLocalObject(updateOrder, localObject.get());
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
