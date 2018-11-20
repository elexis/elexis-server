//package info.elexis.server.core.connector.elexis.services;
//
//public class PrescriptionService extends PersistenceService {
//
//	/**
//	 * Find all active, medical relevant prescriptions, for patient. This only
//	 * includes entries of {@link EntryType#FIXED_MEDICATION},
//	 * {@link EntryType#RESERVE_MEDICATION} and
//	 * {@link EntryType#SYMPTOMATIC_MEDICATION}
//	 * 
//	 * @param patient
//	 * @return
//	 */
////	public static List<Prescription> findAllNonDeletedPrescriptionsForPatient(Kontakt patient) {
////		JPAQuery<Prescription> qbe = new JPAQuery<Prescription>(Prescription.class);
////		qbe.add(Prescription_.patient, JPAQuery.QUERY.EQUALS, patient);
////		// as a boost, if this is set it always refers to a recipe or
////		// dispensation
////		qbe.add(Prescription_.rezeptID, JPAQuery.QUERY.EQUALS, null);
////		List<Prescription> result = qbe.execute();
////		return result.stream()
////				.filter(p -> (EntryType.FIXED_MEDICATION == p.getEntryType()
////						|| EntryType.RESERVE_MEDICATION == p.getEntryType()
////						|| EntryType.SYMPTOMATIC_MEDICATION == p.getEntryType()))
////				.collect(Collectors.toList());
////	}
//
//}
