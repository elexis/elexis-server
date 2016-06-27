package es.fhir.rest.core.resources;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt_;
import info.elexis.server.core.connector.elexis.services.JPAQuery;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;
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

	@Read
	public Patient getResourceById(@IdParam IdDt theId) {
		String idPart = theId.getIdPart();
		if(idPart != null) {
			Optional<Kontakt> patient = KontaktService.INSTANCE.findById(idPart);
			if (patient.isPresent()) {
				if (patient.get().isPatient()) {
					Patient fhirPatient = patientMapper.getFhirObject(patient.get());
					return fhirPatient;
				}
			}
		}
		return null;
	}

	@Search()
	public List<Patient> findPatientByPatientNumber(@RequiredParam(name = "patientNumber") String patientNumber) {
		if (patientNumber != null) {
			Optional<Kontakt> patient = KontaktService.findPatientByPatientNumber(Integer.valueOf(patientNumber));
			if (patient.isPresent()) {
				if (patient.get().isPatient()) {
					Patient fhirPatient = patientMapper.getFhirObject(patient.get());
					return Collections.singletonList(fhirPatient);
				}
			}
		}
		return null;
	}

	@Search()
	public List<Patient> findPatient(@RequiredParam(name = Patient.SP_NAME) String name) {
		if (name != null) {
			JPAQuery<Kontakt> query = new JPAQuery<>(Kontakt.class);
			query.add(Kontakt_.description1, QUERY.LIKE, "%" + name + "%");
			query.or(Kontakt_.description2, QUERY.LIKE, "%" + name + "%");
			query.add(Kontakt_.person, QUERY.EQUALS, true);
			query.add(Kontakt_.patient, QUERY.EQUALS, true);
			List<Kontakt> patients = query.execute();
			if (!patients.isEmpty()) {
				List<Patient> result = patients.stream().map(f -> patientMapper.getFhirObject(f))
						.collect(Collectors.toList());
				return result;
			}
		}
		return null;
	}
}
