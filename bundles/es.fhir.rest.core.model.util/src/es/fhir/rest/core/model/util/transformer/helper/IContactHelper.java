package es.fhir.rest.core.model.util.transformer.helper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.dstu3.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.HumanName.NameUse;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.StringType;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.types.Gender;
import info.elexis.server.core.connector.elexis.services.UserService;

public class IContactHelper extends AbstractHelper {

	private IModelService modelService;

	public IContactHelper(IModelService modelService){
		this.modelService = modelService;
	}

	public List<HumanName> getHumanNames(IPerson person) {
		List<HumanName> ret = new ArrayList<>();
		if (person.isPerson()) {
			HumanName humanName = new HumanName();
			humanName.setFamily(person.getLastName());
			humanName.addGiven(person.getFirstName());
			humanName.addPrefix(person.getTitel());
			humanName.addSuffix(person.getTitelSuffix());
			humanName.setUse(NameUse.OFFICIAL);
			ret.add(humanName);
		}
		if (person.isUser()) {
			Optional<IUser> userLocalObject = UserService.findByContact(person);
			if (userLocalObject.isPresent()) {
				HumanName sysName = new HumanName();
				sysName.setText(userLocalObject.get().getId());
				sysName.setUse(NameUse.ANONYMOUS);
				ret.add(sysName);
			}
		}
		return ret;
	}

	public String getOrganizationName(IOrganization organization) {
		StringBuilder sb = new StringBuilder();
		if (organization.isOrganization()) {
			if (organization.getDescription1() != null) {
				sb.append(organization.getDescription1());
			}
			if (organization.getDescription2() != null) {
				if (sb.length() > 0) {
					sb.append(" ");
				}
				sb.append(organization.getDescription2());
			}
		}
		return sb.toString();
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

	public Date getBirthDate(IPerson kontakt) {
		LocalDateTime dateOfBirth = kontakt.getDateOfBirth();
		if (dateOfBirth != null) {
			return getDate(dateOfBirth);
		}
		return null;
	}

	public List<Address> getAddresses(IContact contact) {
		List<Address> ret = new ArrayList<>();
		if(contact.getCity() != null && !contact.getCity().isEmpty()) {
			Address address = new Address();
			address.setCity(contact.getCity());
			address.setPostalCode(contact.getZip());
			List<StringType> lines = new ArrayList<StringType>();
			lines.add(new StringType(contact.getStreet()));
			address.setLine(lines);
			ret.add(address);
		}
		return ret;
	}

	public List<ContactPoint> getContactPoints(IContact contact) {
		List<ContactPoint> ret = new ArrayList<>();
		if (contact.getPhone1() != null && !contact.getPhone1().isEmpty()) {
			ContactPoint contactPoint = new ContactPoint();
			contactPoint.setSystem(ContactPointSystem.PHONE);
			contactPoint.setUse(ContactPointUse.HOME);
			contactPoint.setValue(contact.getPhone1());
			ret.add(contactPoint);
		}
		if (contact.getPhone2() != null && !contact.getPhone2().isEmpty()) {
			ContactPoint contactPoint = new ContactPoint();
			contactPoint.setSystem(ContactPointSystem.PHONE);
			contactPoint.setUse(ContactPointUse.HOME);
			contactPoint.setValue(contact.getPhone2());
			ret.add(contactPoint);
		}
		if (contact.getMobile() != null && !contact.getMobile().isEmpty()) {
			ContactPoint contactPoint = new ContactPoint();
			contactPoint.setSystem(ContactPointSystem.PHONE);
			contactPoint.setUse(ContactPointUse.MOBILE);
			contactPoint.setValue(contact.getMobile());
			ret.add(contactPoint);
		}
		if (contact.getEmail() != null && !contact.getEmail().isEmpty()) {
			ContactPoint contactPoint = new ContactPoint();
			contactPoint.setSystem(ContactPointSystem.EMAIL);
			contactPoint.setValue(contact.getEmail());
			ret.add(contactPoint);
		}
		if (contact.getWebsite() != null && !contact.getWebsite().isEmpty()) {
			ContactPoint contactPoint = new ContactPoint();
			contactPoint.setSystem(ContactPointSystem.OTHER);
			contactPoint.setValue(contact.getWebsite());
			ret.add(contactPoint);
		}
		return ret;
	}

	public List<Identifier> getIdentifiers(IContact contact) {
		List<Identifier> ret = new ArrayList<>();
		
		IQuery<IXid> query = modelService.getQuery(IXid.class);
		query.and(ModelPackage.Literals.IXID__OBJECT_ID, COMPARATOR.EQUALS, contact.getId());
		// TODO type?
		List<IXid> xids = query.execute();
		for (IXid xid : xids) {
			Identifier identifier = new Identifier();
			identifier.setSystem(xid.getDomain());
			identifier.setValue(xid.getDomainId());
			ret.add(identifier);
		}
		return ret;
	}
}
