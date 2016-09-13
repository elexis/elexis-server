package es.fhir.rest.core.transformer;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.dstu3.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.StringType;

import ch.elexis.core.types.Gender;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Xid;

public class KontaktHelper {

	public List<HumanName> getHumanNames(Kontakt kontakt) {
		List<HumanName> ret = new ArrayList<>();
		if (kontakt.isPerson()) {
			HumanName humanName = new HumanName();
			humanName.addFamily(kontakt.getFamilyName());
			humanName.addGiven(kontakt.getFirstName());
			humanName.addPrefix(kontakt.getTitel());
			humanName.addSuffix(kontakt.getTitelSuffix());
			ret.add(humanName);
		}
		return ret;
	}

	public String getOrganizationName(Kontakt kontakt) {
		if (kontakt.isOrganisation()) {
			return kontakt.getDescription1() + " " + kontakt.getDescription2();
		}
		return "No Organization";
	}

	public AdministrativeGender getGender(Gender gender) {
		if (gender == Gender.FEMALE) {
			return AdministrativeGender.FEMALE;
		} else if (gender == Gender.MALE) {
			return AdministrativeGender.MALE;
		} else if (gender == Gender.UNDEFINED) {
			return AdministrativeGender.OTHER;
		} else {
			return AdministrativeGender.UNKNOWN;
		}
	}

	public Date getBirthDate(Kontakt kontakt) {
		LocalDate dateOfBirth = kontakt.getDob();
		if (dateOfBirth != null) {
			return Date.from(dateOfBirth.atStartOfDay(ZoneId.systemDefault()).toInstant());
		}
		return null;
	}

	public List<Address> getAddresses(Kontakt kontakt) {
		List<Address> ret = new ArrayList<>();
		if(kontakt.getCity() != null && !kontakt.getCity().isEmpty()) {
			Address address = new Address();
			address.setCity(kontakt.getCity());
			address.setPostalCode(kontakt.getZip());
			List<StringType> lines = new ArrayList<StringType>();
			lines.add(new StringType(kontakt.getStreet()));
			address.setLine(lines);
			ret.add(address);
		}
		return ret;
	}

	public List<ContactPoint> getContactPoints(Kontakt kontakt) {
		List<ContactPoint> ret = new ArrayList<>();
		if (kontakt.getPhone1() != null && !kontakt.getPhone1().isEmpty()) {
			ContactPoint contactPoint = new ContactPoint();
			contactPoint.setSystem(ContactPointSystem.PHONE);
			contactPoint.setUse(ContactPointUse.HOME);
			contactPoint.setValue(kontakt.getPhone1());
			ret.add(contactPoint);
		}
		if (kontakt.getPhone2() != null && !kontakt.getPhone2().isEmpty()) {
			ContactPoint contactPoint = new ContactPoint();
			contactPoint.setSystem(ContactPointSystem.PHONE);
			contactPoint.setUse(ContactPointUse.HOME);
			contactPoint.setValue(kontakt.getPhone2());
			ret.add(contactPoint);
		}
		if (kontakt.getMobile() != null && !kontakt.getMobile().isEmpty()) {
			ContactPoint contactPoint = new ContactPoint();
			contactPoint.setSystem(ContactPointSystem.PHONE);
			contactPoint.setUse(ContactPointUse.MOBILE);
			contactPoint.setValue(kontakt.getMobile());
			ret.add(contactPoint);
		}
		if (kontakt.getEmail() != null && !kontakt.getEmail().isEmpty()) {
			ContactPoint contactPoint = new ContactPoint();
			contactPoint.setSystem(ContactPointSystem.EMAIL);
			contactPoint.setValue(kontakt.getEmail());
			ret.add(contactPoint);
		}
		if (kontakt.getWebsite() != null && !kontakt.getWebsite().isEmpty()) {
			ContactPoint contactPoint = new ContactPoint();
			contactPoint.setSystem(ContactPointSystem.OTHER);
			contactPoint.setValue(kontakt.getWebsite());
			ret.add(contactPoint);
		}
		return ret;
	}

	public List<Identifier> getIdentifiers(Kontakt kontakt) {
		List<Identifier> ret = new ArrayList<>();
		System.out.println("Get Xids [" + kontakt.getId() + "]");
		Collection<Xid> xids = kontakt.getXids().values();
		for (Xid xid : xids) {
			System.out.println("XID Domain " + xid.getDomain() + " ID " + xid.getDomainId());
			Identifier identifier = new Identifier();
			identifier.setSystem(xid.getDomain());
			identifier.setValue(xid.getDomainId());
			ret.add(identifier);
		}
		return ret;
	}
}
