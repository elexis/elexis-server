package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.dstu2.resource.MedicationOrder;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription;
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
	public MedicationOrder getResourceById(@IdParam IdDt theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<Prescription> prescription = PrescriptionService.INSTANCE.findById(idPart);
			if (prescription.isPresent()) {
				MedicationOrder fhirMedicationOrder = prescriptionMapper.getFhirObject(prescription.get());
				return fhirMedicationOrder;
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
					List<Prescription> prescriptions = PrescriptionService
							.findAllNonDeletedPrescriptionsForPatient(patient.get());
					if (prescriptions != null && !prescriptions.isEmpty()) {
						List<MedicationOrder> ret = new ArrayList<MedicationOrder>();
						for (Prescription prescription : prescriptions) {
							ret.add(prescriptionMapper.getFhirObject(prescription));
						}
						return ret;
					}
				}
			}
		}
		return null;
	}
}
