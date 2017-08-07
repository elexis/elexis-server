package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IContact;
import ch.elexis.core.types.Gender;
import ch.elexis.core.types.RelationshipType;
import info.elexis.server.core.connector.elexis.jpa.ElexisTypeMap;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.DbImage;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.KontaktAdressJoint;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.listener.KontaktEntityListener;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.types.MimeType;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;

public class KontaktService extends PersistenceService {

	private static Logger log = LoggerFactory.getLogger(KontaktService.class);

	public static class PersonBuilder extends AbstractBuilder<Kontakt> {
		public PersonBuilder(String firstName, String lastName, LocalDate dateOfBirth, Gender sex) {
			object = new Kontakt();
			object.setDescription1(lastName);
			object.setDescription2(firstName);
			object.setDob(dateOfBirth);
			object.setGender(sex);
			object.setPerson(true);
		}

		/**
		 * This method does not initialize a patient number. The patient number is
		 * created by the persistence layer in {@link KontaktEntityListener}
		 * 
		 * @return
		 */
		public PersonBuilder patient() {
			object.setPatient(true);
			return this;
		}

		public PersonBuilder mandator() {
			object.setMandator(true);
			return this;
		}
	}

	public static class OrganizationBuilder extends AbstractBuilder<Kontakt> {
		public OrganizationBuilder(String name) {
			object = new Kontakt();
			object.setDescription1(name);
			object.setOrganisation(true);
		}

		public OrganizationBuilder laboratory() {
			object.setLaboratory(true);
			return this;
		}
	}

	/**
	 * convenience method
	 * 
	 * @param id
	 * @return
	 */
	public static Optional<Kontakt> load(String id) {
		return PersistenceService.load(Kontakt.class, id).map(v -> (Kontakt) v);
	}

	/**
	 * Reload the entity from the database. Convenience method.
	 * 
	 * @param id
	 * @return
	 */
	public static Kontakt reload(Kontakt contact) {
		return PersistenceService.reload(contact).map(v -> (Kontakt) v)
				.orElseThrow(() -> new IllegalStateException("reload failed"));
	}

	public static List<Fall> getFaelle(Kontakt k) {
		JPAQuery<Fall> query = new JPAQuery<Fall>(Fall.class);
		query.add(Fall_.patientKontakt, QUERY.EQUALS, k);
		return query.execute();
	}

	/**
	 * Set a profile image for a given contact.
	 * 
	 * @param contact
	 * @param image
	 * @param mimeType 
	 */
	public static void setContactImage(Kontakt contact, byte[] image, MimeType mimeType) {
		@SuppressWarnings("unchecked")
		Optional<DbImage> contactImage = (Optional<DbImage>) PersistenceService.load(DbImage.class, contact.getId());
		DbImage dbImage = new DbImage();
		if (!contactImage.isPresent()) {
			dbImage = new DbImage();
			dbImage.setId(contact.getId());
		} else {
			dbImage = (DbImage) contactImage.get();
		}

		dbImage.setPrefix(ElexisTypeMap.TYPE_KONTAKT);
		dbImage.setTitle("ContactImage."+mimeType.name());
		dbImage.setImage(image);
		PersistenceService.save(dbImage);
	}

	/**
	 * 
	 * @param contact
	 * @return the {@link DbImage} object containing the resp. image, if present
	 */
	@SuppressWarnings("unchecked")
	public static Optional<DbImage> getContactImage(Kontakt contact) {
		return (Optional<DbImage>) PersistenceService.load(DbImage.class, contact.getId());
	}

	/**
	 * 
	 * @param k
	 * @return the age in years, or -1 if not applicable
	 */
	public static int getAgeInYears(Kontakt k) {
		LocalDate dob = k.getDob();
		if (dob == null) {
			return -1;
		}

		LocalDate now = LocalDate.now();
		long years = ChronoUnit.YEARS.between(dob, now);
		return (int) years;
	}

	public static List<Kontakt> findAllPatients() {
		JPAQuery<Kontakt> query = new JPAQuery<Kontakt>(Kontakt.class);
		query.add(Kontakt_.person, QUERY.EQUALS, true);
		query.add(Kontakt_.patient, QUERY.EQUALS, true);
		return query.execute();
	}

	public static Optional<Kontakt> findPatientByPatientNumber(int randomPatientNumber) {
		JPAQuery<Kontakt> query = new JPAQuery<Kontakt>(Kontakt.class);
		query.add(Kontakt_.person, QUERY.EQUALS, true);
		query.add(Kontakt_.patient, QUERY.EQUALS, true);
		query.add(Kontakt_.code, QUERY.EQUALS, Integer.toString(randomPatientNumber));
		return query.executeGetSingleResult();
	}

	public static Optional<IContact> findLaboratory(String identifier) {
		if (identifier == null || identifier.isEmpty()) {
			throw new IllegalArgumentException("Labor identifier [" + identifier + "] invalid.");
		}

		IContact laboratory = null;
		JPAQuery<Kontakt> query = new JPAQuery<Kontakt>(Kontakt.class);
		query.add(Kontakt_.code, QUERY.LIKE, "%" + identifier + "%");
		query.or(Kontakt_.description1, QUERY.LIKE, "%" + identifier + "%");
		List<Kontakt> results = query.execute();
		if (results.isEmpty()) {
			log.warn("Found no Labor for identifier [" + identifier + "]. Created new Labor contact.");
			return Optional.empty();
		} else {
			laboratory = results.get(0);
			if (results.size() > 1) {
				log.warn("Found more than one Labor for identifier [" + identifier
						+ "]. This can cause problems when importing results.");
			}
		}
		return Optional.ofNullable(laboratory);
	}

	// TODO normalize
	public static List<Kontakt> findPersonByMultipleOptionalParameters(String firstName, String lastName, Gender gender,
			String city, String street, String zip) {
		JPAQuery<Kontakt> query = new JPAQuery<Kontakt>(Kontakt.class);
		query.add(Kontakt_.person, QUERY.EQUALS, true);
		if (firstName != null) {
			// TODO TEST
			query.addLikeNormalized(Kontakt_.description1, "%" + firstName.trim() + "%");
		}
		if (lastName != null) {
			query.add(Kontakt_.description2, QUERY.LIKE, "%" + lastName.trim() + "%");
		}
		if (gender != null) {
			query.add(Kontakt_.gender, QUERY.EQUALS, gender);
		}
		if (city != null) {
			query.add(Kontakt_.city, QUERY.LIKE, "%" + city.trim() + "%");
		}
		if (street != null) {
			query.add(Kontakt_.street, QUERY.LIKE, "%" + street.trim() + "%");
		}
		if (zip != null) {
			query.add(Kontakt_.zip, QUERY.LIKE, "%" + zip.trim() + "%");
		}
		return query.execute();
	}

	/**
	 * Set a related contact to the provided myContact. If there already exists a
	 * related contact where the otherContact, otherContactrole and (if defined)
	 * relationshipDescription matches, no additional entry is done.
	 * 
	 * @param myContact
	 * @param otherContact
	 * @param otherContactRole
	 *            a formal {@link RelationshipType} int value, <code>null</code>
	 *            valid
	 * @param myContactRole
	 *            a formal {@link RelationshipType} int value, <code>null</code>
	 *            valid
	 * @param relationshipDescription
	 *            <code>null</code> or a description
	 * @return
	 */
	public static KontaktAdressJoint setRelatedContact(Kontakt myContact, Kontakt otherContact, Integer otherContactRole,
			Integer myContactRole, String relationshipDescription) {
		
		if (otherContactRole == null) {
			otherContactRole = RelationshipType.AGENERIC_VALUE;
		}
		if (myContactRole == null) {
			myContactRole = RelationshipType.AGENERIC_VALUE;
		}
		
		// other contact and role or roleDescription already found ?
		// return
		Collection<KontaktAdressJoint> relatedContacts = myContact.getRelatedContacts().values();
		for (KontaktAdressJoint kaj : relatedContacts) {
			if (otherContact.equals(kaj.getOtherKontakt()) && kaj.getOtherRType() == otherContactRole) {
				if (relationshipDescription != null) {
					if (relationshipDescription.equals(kaj.getBezug())) {
						return kaj;
					}
				} else {
					return kaj;
				}
			}
		}

		KontaktAdressJoint relatedContact = new KontaktAdressJoint();
		relatedContact.setMyKontakt(myContact);
		relatedContact.setOtherKontakt(otherContact);
		relatedContact.setOtherRType(otherContactRole);
		relatedContact.setMyRType(myContactRole);
		relatedContact.setBezug(relationshipDescription);
		myContact.getRelatedContacts().put(relatedContact.getId(), relatedContact);
		return relatedContact;
	}

}
