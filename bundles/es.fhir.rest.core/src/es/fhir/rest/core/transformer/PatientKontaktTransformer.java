package es.fhir.rest.core.transformer;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.types.Gender;
import es.fhir.rest.core.IFhirTransformer;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

@Component
public class PatientKontaktTransformer implements IFhirTransformer<Patient, Kontakt> {

	@Override
	public Optional<Patient> getFhirObject(Kontakt localObject) {
		Patient patient = new Patient();

		patient.setId(new IdDt("Patient", localObject.getId()));

		String patNr = localObject.getPatientNr();
		Identifier elexisId = patient.addIdentifier();
		elexisId.setSystem("www.elexis.info/patnr");
		elexisId.setValue(patNr);

		elexisId = patient.addIdentifier();
		elexisId.setSystem("www.elexis.info/objid");
		elexisId.setValue(localObject.getId());

		HumanName patName = patient.addName();
		patName.addFamily(localObject.getFamilyName());
		patName.addGiven(localObject.getFirstName());
		patName.addPrefix(localObject.getTitel());
		patName.addSuffix(localObject.getTitelSuffix());

		if (localObject.getGender() == Gender.FEMALE) {
			patient.setGender(AdministrativeGender.FEMALE);
		} else if (localObject.getGender() == Gender.MALE) {
			patient.setGender(AdministrativeGender.MALE);
		} else if (localObject.getGender() == Gender.UNDEFINED) {
			patient.setGender(AdministrativeGender.OTHER);
		} else {
			patient.setGender(AdministrativeGender.UNKNOWN);
		}

		LocalDate dateOfBirth = localObject.getDob();
		if (dateOfBirth != null) {
			patient.setBirthDate(Date.from(dateOfBirth.atStartOfDay(ZoneId.systemDefault()).toInstant()));
		}

		return Optional.of(patient);
	}

	@Override
	public Optional<Kontakt> getLocalObject(Patient fhirObject) {
		return Optional.empty();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Patient.class.equals(fhirClazz) && Kontakt.class.equals(localClazz);
	}

	@Override
	public void updateLocalObject(Patient fhirObject, Kontakt localObject) {
		// TODO Auto-generated method stub

	}

	@Override
	public Optional<Kontakt> createLocalObject(Patient fhirObject) {
		return Optional.empty();
	}
}
