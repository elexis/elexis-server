package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ch.elexis.core.findings.IdentifierSystem;
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

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Patient.class;
	}

	private IFhirTransformerRegistry transformerRegistry;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFhirTransformerRegistry(IFhirTransformerRegistry transformerRegistry) {
		this.transformerRegistry = transformerRegistry;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Patient, Kontakt> getTransformer() {
		return (IFhirTransformer<Patient, Kontakt>) transformerRegistry.getTransformerFor(Patient.class, Kontakt.class);
	}

	@Read
	public Patient getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<Kontakt> patient = KontaktService.load(idPart);
			if (patient.isPresent()) {
				if (patient.get().isPatient()) {
					Optional<Patient> fhirPatient = getTransformer().getFhirObject(patient.get());
					return fhirPatient.get();
				}
			}
		}
		return null;
	}

	@Search()
	public List<Patient> findPatientByIdentifier(@RequiredParam(name = Patient.SP_IDENTIFIER) IdentifierDt identifier) {
		if (identifier != null) {
			if (identifier.getSystem().equals(IdentifierSystem.ELEXIS_PATNR.getSystem())) {
				Optional<Kontakt> patient = KontaktService
						.findPatientByPatientNumber(Integer.valueOf(identifier.getValue()));
				if (patient.isPresent()) {
					if (patient.get().isPatient()) {
						Optional<Patient> fhirPatient = getTransformer().getFhirObject(patient.get());
						return Collections.singletonList(fhirPatient.get());
					}
				}
			}
		}
		return Collections.emptyList();
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
				List<Patient> ret = new ArrayList<Patient>();
				for (Kontakt patient : patients) {
					Optional<Patient> fhirPatient = getTransformer().getFhirObject(patient);
					fhirPatient.ifPresent(fp -> ret.add(fp));
				}
				return ret;
			}
		}
		return Collections.emptyList();
	}
}
