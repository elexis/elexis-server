package es.fhir.rest.core.model.util.transformer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.StringType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.findings.IdentifierSystem;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IModelService;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.model.util.transformer.helper.IContactHelper;

@Component
public class PatientIPatientTransformer implements IFhirTransformer<Patient, IPatient> {
	
	@Reference
	IModelService modelService;

	private IContactHelper contactHelper = new IContactHelper(modelService);

	@Override
	public Optional<Patient> getFhirObject(IPatient localObject, Set<Include> includes) {
		Patient patient = new Patient();

		patient.setId(new IdDt("Patient", localObject.getId()));

		List<Identifier> identifiers = contactHelper.getIdentifiers(localObject);
		identifiers.add(getElexisObjectIdentifier(localObject));
		String patNr = localObject.getPatientNr();
		Identifier identifier = new Identifier();
		identifier.setSystem(IdentifierSystem.ELEXIS_PATNR.getSystem());
		identifier.setValue(patNr);
		identifiers.add(identifier);
		patient.setIdentifier(identifiers);

		patient.setName(contactHelper.getHumanNames(localObject));
		patient.setGender(contactHelper.getGender(localObject.getGender()));
		patient.setBirthDate(contactHelper.getBirthDate(localObject));
		List<Address> addresses = contactHelper.getAddresses(localObject);
		patient.setAddress(addresses);
		List<ContactPoint> contactPoints = contactHelper.getContactPoints(localObject);
		patient.setTelecom(contactPoints);

		Extension elexisPatientNote = new Extension();
		elexisPatientNote.setUrl("www.elexis.info/extensions/patient/notes");
		elexisPatientNote.setValue(new StringType(localObject.getComment()));
		patient.addExtension(elexisPatientNote);

		return Optional.of(patient);
	}

	@Override
	public Optional<IPatient> getLocalObject(Patient fhirObject) {
		String id = fhirObject.getIdElement().getIdPart();
		if (id != null && !id.isEmpty()) {
			return modelService.load(id, IPatient.class);
		}
		return Optional.empty();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Patient.class.equals(fhirClazz) && IPatient.class.equals(localClazz);
	}

	@Override
	public Optional<IPatient> updateLocalObject(Patient fhirObject, IPatient localObject) {
		return Optional.empty();
	}

	@Override
	public Optional<IPatient> createLocalObject(Patient fhirObject) {
		return Optional.empty();
	}
}
