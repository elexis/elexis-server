package es.fhir.rest.core.model.util.transformer.mapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.Address.AddressUse;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.dstu3.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.StringType;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.findings.IdentifierSystem;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.types.Country;
import ch.elexis.core.types.Gender;
import es.fhir.rest.core.model.util.transformer.helper.IContactHelper;

public class IPatientPatientAttributeMapper {

	private IContactHelper contactHelper;

	public IPatientPatientAttributeMapper(IContactHelper contactHelper) {
		this.contactHelper = contactHelper;
	}

	public void elexisToFhir(IPatient source, Patient target) {
		target.setId(new IdDt("Patient", source.getId()));
		mapMetaData(source, target);
		
		mapIdentifiersAndPatientNumber(source, target);
		target.setName(contactHelper.getHumanNames(source));
		target.setGender(contactHelper.getGender(source.getGender()));
		target.setBirthDate(contactHelper.getBirthDate(source));
		mapAddressTelecom(source, target);
		mapComments(source, target);
	}

	public void fhirToElexis(Patient source, IPatient target) {
		// id must not be mapped (not updateable)
		// patientNumber must not be mapped (not updateable)
		mapName(source, target);
		mapGender(source, target);
		mapBirthDate(source, target);
		mapAddressTelecom(source, target);
		mapComments(source, target);
	}
	
	private void mapMetaData(IPatient source, Patient target) {
		Meta meta = new Meta();
		meta.setLastUpdated(new Date(source.getLastupdate()));
		target.setMeta(meta);
	}

	private void mapComments(Patient source, IPatient target) {
		List<Extension> extensionsByUrl = source.getExtensionsByUrl("www.elexis.info/extensions/patient/notes");
		if (!extensionsByUrl.isEmpty()) {
			target.setComment(extensionsByUrl.get(0).getValue().toString());
		}
	}

	private void mapComments(IPatient source, Patient target) {
		Extension elexisPatientNote = new Extension();
		elexisPatientNote.setUrl("www.elexis.info/extensions/patient/notes");
		elexisPatientNote.setValue(new StringType(source.getComment()));
		target.addExtension(elexisPatientNote);
	}

	private void mapAddressTelecom(IPatient source, Patient target) {
		List<Address> addresses = contactHelper.getAddresses(source);
		target.setAddress(addresses);
		List<ContactPoint> contactPoints = contactHelper.getContactPoints(source);
		target.setTelecom(contactPoints);
	}

	private void mapAddressTelecom(Patient source, IPatient target) {
		List<Address> addresses = source.getAddress();
		for (Address address : addresses) {
			if (AddressUse.HOME.equals(address.getUse())) {
				target.setCity(address.getCity());
				target.setZip(address.getPostalCode());
				if (!address.getLine().isEmpty()) {
					target.setStreet(address.getLine().get(0).asStringValue());
				}
				Country country = null;
				try {
					country = Country.valueOf(address.getCountry());
				} catch (IllegalArgumentException | NullPointerException e) {
					// ignore
				}
				target.setCountry(country);
			}
		}

		List<ContactPoint> telecoms = source.getTelecom();
		for (ContactPoint contactPoint : telecoms) {
			if (ContactPointSystem.PHONE.equals(contactPoint.getSystem())) {
				if (ContactPointUse.MOBILE.equals(contactPoint.getUse())) {
					target.setMobile(contactPoint.getValue());
				}
			}
			if (ContactPointSystem.EMAIL.equals(contactPoint.getSystem())) {
				target.setEmail(contactPoint.getValue());
			}
		}
	}

	private void mapBirthDate(Patient source, IPatient target) {
		if (source.getBirthDate() != null) {
			LocalDateTime dob = source.getBirthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			target.setDateOfBirth(dob);
		}
	}

	private void mapGender(Patient source, IPatient target) {
		AdministrativeGender gender = source.getGender();
		if(gender != null) {
			switch (gender) {
			case FEMALE:
				target.setGender(Gender.FEMALE);
				break;
			case MALE:
				target.setGender(Gender.MALE);
				break;
			case UNKNOWN:
				target.setGender(Gender.UNKNOWN);
				break;
			default:
				target.setGender(Gender.UNDEFINED);
			}
		}
	}

	private void mapName(Patient source, IPatient target) {
		List<HumanName> names = source.getName();
		for (HumanName humanName : names) {
			if (HumanName.NameUse.OFFICIAL.equals(humanName.getUse())) {
				target.setFirstName(humanName.getGivenAsSingleString());
				target.setLastName(humanName.getFamily());
				target.setTitel(humanName.getPrefixAsSingleString());
				target.setTitelSuffix(humanName.getSuffixAsSingleString());
			}
		}
	}

	private void mapIdentifiersAndPatientNumber(IPatient source, Patient target) {
		List<Identifier> identifiers = contactHelper.getIdentifiers(source);
		identifiers.add(getElexisObjectIdentifier(source));
		String patNr = source.getPatientNr();
		Identifier identifier = new Identifier();
		identifier.setSystem(IdentifierSystem.ELEXIS_PATNR.getSystem());
		identifier.setValue(patNr);
		identifiers.add(identifier);
		target.setIdentifier(identifiers);
	}

	private Identifier getElexisObjectIdentifier(Identifiable dbObject) {
		Identifier identifier = new Identifier();
		identifier.setSystem(IdentifierSystem.ELEXIS_OBJID.getSystem());
		identifier.setValue(dbObject.getId());
		return identifier;
	}

}