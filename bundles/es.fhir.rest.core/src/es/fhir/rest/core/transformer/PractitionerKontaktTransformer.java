package es.fhir.rest.core.transformer;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.types.Gender;
import es.fhir.rest.core.IFhirTransformer;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

@Component
public class PractitionerKontaktTransformer implements IFhirTransformer<Practitioner, Kontakt> {

	@Override
	public Optional<Practitioner> getFhirObject(Kontakt localObject) {
		Practitioner practitioner = new Practitioner();

		practitioner.setId(new IdDt("Practitioner", localObject.getId()));
		Identifier elexisId = practitioner.addIdentifier();

		elexisId.setSystem("www.elexis.info/objid");
		elexisId.setValue(localObject.getId());

		HumanName practitionerName = practitioner.addName();
		practitionerName.addFamily(localObject.getFamilyName());
		practitionerName.addGiven(localObject.getFirstName());
		practitionerName.addPrefix(localObject.getTitel());
		practitionerName.addSuffix(localObject.getTitelSuffix());

		if (localObject.getGender() == Gender.FEMALE) {
			practitioner.setGender(AdministrativeGender.FEMALE);
		} else if (localObject.getGender() == Gender.MALE) {
			practitioner.setGender(AdministrativeGender.MALE);
		} else if (localObject.getGender() == Gender.UNDEFINED) {
			practitioner.setGender(AdministrativeGender.OTHER);
		} else {
			practitioner.setGender(AdministrativeGender.UNKNOWN);
		}

		LocalDate dateOfBirth = localObject.getDob();
		if (dateOfBirth != null) {
			practitioner.setBirthDate(Date.from(dateOfBirth.atStartOfDay(ZoneId.systemDefault()).toInstant()));
		}

		return Optional.of(practitioner);
	}

	@Override
	public Optional<Kontakt> getLocalObject(Practitioner fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Kontakt> updateLocalObject(Practitioner fhirObject, Kontakt localObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Kontakt> createLocalObject(Practitioner fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Practitioner.class.equals(fhirClazz) && Kontakt.class.equals(localClazz);
	}
}
