package es.fhir.rest.core.transformer;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.primitive.IdDt;
import es.fhir.rest.core.IFhirTransformer;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.KontaktService;

@Component
public class PatientKontaktTransformer implements IFhirTransformer<Patient, Kontakt> {

	private KontaktHelper kontaktHelper = new KontaktHelper();

	@Override
	public Optional<Patient> getFhirObject(Kontakt localObject) {
		Patient patient = new Patient();

		patient.setId(new IdDt("Patient", localObject.getId()));

		List<Identifier> identifiers = kontaktHelper.getIdentifiers(localObject);
		identifiers.add(getElexisObjectIdentifier(localObject));
		String patNr = localObject.getPatientNr();
		Identifier identifier = new Identifier();
		identifier.setSystem("www.elexis.info/patnr");
		identifier.setValue(patNr);
		identifiers.add(identifier);
		patient.setIdentifier(identifiers);

		patient.setName(kontaktHelper.getHumanNames(localObject));
		patient.setGender(kontaktHelper.getGender(localObject.getGender()));
		patient.setBirthDate(kontaktHelper.getBirthDate(localObject));
		List<Address> addresses = kontaktHelper.getAddresses(localObject);
		patient.setAddress(addresses);
		List<ContactPoint> contactPoints = kontaktHelper.getContactPoints(localObject);
		patient.setTelecom(contactPoints);

		return Optional.of(patient);
	}

	@Override
	public Optional<Kontakt> getLocalObject(Patient fhirObject) {
		String id = fhirObject.getIdElement().getIdPart();
		if (id != null && !id.isEmpty()) {
			return KontaktService.INSTANCE.findById(id);
		}
		return Optional.empty();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Patient.class.equals(fhirClazz) && Kontakt.class.equals(localClazz);
	}

	@Override
	public Optional<Kontakt> updateLocalObject(Patient fhirObject, Kontakt localObject) {
		return Optional.empty();
	}

	@Override
	public Optional<Kontakt> createLocalObject(Patient fhirObject) {
		return Optional.empty();
	}
}
