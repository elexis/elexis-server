package es.fhir.rest.core.transformer;

import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.UriDt;
import ch.elexis.core.types.Gender;
import es.fhir.rest.core.IFhirTransformer;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

@Component
public class PatientKontaktTransformer implements IFhirTransformer<Patient, Kontakt> {

	@Override
	public Patient getFhirObject(Kontakt localObject) {
		Patient patient = new Patient();

		patient.setId(new IdDt("Patient", localObject.getId()));

		String patNr = localObject.getPatientNr();
		IdentifierDt elexisId = patient.addIdentifier();
		elexisId.setSystem(new UriDt("www.elexis.info/patnr"));
		elexisId.setValue(patNr);

		elexisId = patient.addIdentifier();
		elexisId.setSystem(new UriDt("www.elexis.info/objid"));
		elexisId.setValue(localObject.getId());

		HumanNameDt patName = patient.addName();
		patName.addFamily(localObject.getFamilyName());
		patName.addGiven(localObject.getFirstName());
		patName.addPrefix(localObject.getTitel());
		patName.addSuffix(localObject.getTitelSuffix());

		if (localObject.getGender() == Gender.FEMALE) {
			patient.setGender(AdministrativeGenderEnum.FEMALE);
		} else if (localObject.getGender() == Gender.MALE) {
			patient.setGender(AdministrativeGenderEnum.MALE);
		} else if (localObject.getGender() == Gender.UNDEFINED) {
			patient.setGender(AdministrativeGenderEnum.OTHER);
		} else {
			patient.setGender(AdministrativeGenderEnum.UNKNOWN);
		}
		return patient;
	}

	@Override
	public Kontakt getLocalObject(Patient fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Patient.class.equals(fhirClazz) && Kontakt.class.equals(localClazz);
	}
}
