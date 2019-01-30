package info.elexis.server.core.connector.elexis.services;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.types.Gender;
import info.elexis.server.core.connector.elexis.services.internal.CoreModelServiceHolder;

public class ContactService extends PersistenceService2 {
	
	private static Logger log = LoggerFactory.getLogger(ContactService.class);
	
	private static IModelService modelService = CoreModelServiceHolder.get();
	
	public static List<ICoverage> getFaelle(IPatient patient){
		IQuery<ICoverage> query = modelService.getQuery(ICoverage.class);
		query.and(ModelPackage.Literals.ICOVERAGE__PATIENT, COMPARATOR.EQUALS, patient);
		return query.execute();
	}
	
	public static Optional<IPatient> findPatientByPatientNumber(int randomPatientNumber){
		IQuery<IPatient> query = modelService.getQuery(IPatient.class);
		query.and(ModelPackage.Literals.ICONTACT__CODE, COMPARATOR.EQUALS,
			Integer.toString(randomPatientNumber));
		return query.executeSingleResult();
	}
	
	public static Optional<IContact> findLaboratory(String identifier){
		if (identifier == null || identifier.isEmpty()) {
			throw new IllegalArgumentException("Labor identifier [" + identifier + "] invalid.");
		}
		
		IContact laboratory = null;
		IQuery<IContact> query = modelService.getQuery(IContact.class);
		query.and(ModelPackage.Literals.ICONTACT__CODE, COMPARATOR.LIKE, "%" + identifier + "%");
		query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE,
			"%" + identifier + "%");
		List<IContact> results = query.execute();
		if (results.isEmpty()) {
			log.warn(
				"Found no Labor for identifier [" + identifier + "]. Created new Labor contact.");
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
	
	public static List<IContact> findPersonByMultipleOptionalParameters(String firstName,
		String lastName, Gender gender, String city, String street, String zip){
		IQuery<IContact> query = modelService.getQuery(IContact.class);
		query.and(ModelPackage.Literals.ICONTACT__PERSON, COMPARATOR.EQUALS, true);
		if (firstName != null) {
			query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE,
				"%" + firstName.trim() + "%", true);
		}
		if (lastName != null) {
			query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION2, COMPARATOR.LIKE,
				"%" + lastName.trim() + "%", true);
		}
		if (gender != null) {
			query.and(ModelPackage.Literals.IPERSON__GENDER, COMPARATOR.EQUALS, gender);
		}
		if (city != null) {
			query.and(ModelPackage.Literals.ICONTACT__CITY, COMPARATOR.LIKE,
				"%" + city.trim() + "%", true);
		}
		if (street != null) {
			query.and(ModelPackage.Literals.ICONTACT__STREET, COMPARATOR.LIKE,
				"%" + street.trim() + "%", true);
		}
		if (zip != null) {
			query.and(ModelPackage.Literals.ICONTACT__ZIP, COMPARATOR.LIKE, "%" + zip.trim() + "%");
		}
		return query.execute();
	}
	
//	/**
//	 * Set a related contact to the provided myContact. If there already exists a related contact
//	 * where the otherContact, otherContactrole and (if defined) relationshipDescription matches, no
//	 * additional entry is done.
//	 * 
//	 * @param myContact
//	 * @param otherContact
//	 * @param otherContactRole
//	 *            a formal {@link RelationshipType} int value, <code>null</code> valid
//	 * @param myContactRole
//	 *            a formal {@link RelationshipType} int value, <code>null</code> valid
//	 * @param relationshipDescription
//	 *            <code>null</code> or a description
//	 * @return
//	 */
//	public static KontaktAdressJoint setRelatedContact(IContact myContact, IContact otherContact,
//		Integer otherContactRole, Integer myContactRole, String relationshipDescription){
//		
//		if (otherContactRole == null) {
//			otherContactRole = RelationshipType.AGENERIC_VALUE;
//		}
//		if (myContactRole == null) {
//			myContactRole = RelationshipType.AGENERIC_VALUE;
//		}
//		
//		// other contact and role or roleDescription already found ?
//		// return
//		Collection<KontaktAdressJoint> relatedContacts = myContact.getRelatedContacts().values();
//		for (KontaktAdressJoint kaj : relatedContacts) {
//			if (otherContact.equals(kaj.getOtherKontakt())
//				&& kaj.getOtherRType() == otherContactRole) {
//				if (relationshipDescription != null) {
//					if (relationshipDescription.equals(kaj.getBezug())) {
//						return kaj;
//					}
//				} else {
//					return kaj;
//				}
//			}
//		}
//		
//		KontaktAdressJoint relatedContact = new KontaktAdressJoint();
//		relatedContact.setMyKontakt(myContact);
//		relatedContact.setOtherKontakt(otherContact);
//		relatedContact.setOtherRType(otherContactRole);
//		relatedContact.setMyRType(myContactRole);
//		relatedContact.setBezug(relationshipDescription);
//		myContact.getRelatedContacts().put(relatedContact.getId(), relatedContact);
//		return relatedContact;
//	}
	
}
