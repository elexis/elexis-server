package es.fhir.rest.core.resources;

import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.KontaktService;

@Component
public class PatientResourceProvider implements IFhirResourceProvider {

	private IFhirTransformer<Patient, Kontakt> patientMapper;

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Patient.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initTransformer(IFhirTransformerRegistry transformerRegistry) {
		patientMapper = (IFhirTransformer<Patient, Kontakt>) transformerRegistry.getTransformerFor(Patient.class,
				Kontakt.class);
	}

	/**
	 * The "@Read" annotation indicates that this method supports the read
	 * operation. Read operations should return a single resource instance.
	 *
	 * @param theId
	 *            The read operation takes one parameter, which must be of type
	 *            IdDt and must be annotated with the "@Read.IdParam"
	 *            annotation.
	 * @return Returns a resource matching this identifier, or null if none
	 *         exists.
	 */
	@Read
	public Patient getResourceById(@IdParam IdDt theId) {
		Long idPart = theId.getIdPartAsLong();
		if(idPart != null) {
			Optional<Kontakt> patient = KontaktService.findPatientByPatientNumber(idPart.intValue());
			if (patient.isPresent()) {
				if (patient.get().isPatient()) {
					Patient fhirPatient = patientMapper.getFhirObject(patient.get());
					return fhirPatient;
				}
			}
		}
		return null;
	}
}
