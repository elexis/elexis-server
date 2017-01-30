package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ch.elexis.core.model.prescription.EntryType;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription_;

public class PrescriptionService extends PersistenceService {

	public static class Builder extends AbstractBuilder<Prescription> {
		public Builder(AbstractDBObjectIdDeleted article, Kontakt patient, String dosage) {
			object = new Prescription();
			object.setArtikel(article);
			object.setPatient(patient);
			object.setDosis(dosage);
			object.setDateFrom(LocalDateTime.now());
		}
	}

	/**
	 * convenience method
	 * 
	 * @param id
	 * @return
	 */
	public static Optional<Prescription> load(String id) {
		return PersistenceService.load(Prescription.class, id).map(v -> (Prescription) v);
	}

	/**
	 * convenience method
	 * 
	 * @param includeElementsMarkedDeleted
	 * @return
	 */
	public static List<Prescription> findAll(boolean includeElementsMarkedDeleted) {
		return PersistenceService.findAll(Prescription.class, includeElementsMarkedDeleted).stream()
				.map(v -> (Prescription) v).collect(Collectors.toList());
	}

	/**
	 * Find all active, medical relevant prescriptions, for patient. This only
	 * includes entries of {@link EntryType#FIXED_MEDICATION},
	 * {@link EntryType#RESERVE_MEDICATION} and
	 * {@link EntryType#SYMPTOMATIC_MEDICATION}
	 * 
	 * @param patient
	 * @return
	 */
	public static List<Prescription> findAllNonDeletedPrescriptionsForPatient(Kontakt patient) {
		JPAQuery<Prescription> qbe = new JPAQuery<Prescription>(Prescription.class);
		qbe.add(Prescription_.patient, JPAQuery.QUERY.EQUALS, patient);
		// as a boost, if this is set it always refers to a recipe or
		// dispensation
		qbe.add(Prescription_.rezeptID, JPAQuery.QUERY.EQUALS, null);
		List<Prescription> result = qbe.execute();
		return result.stream()
				.filter(p -> (EntryType.FIXED_MEDICATION == p.getEntryType()
						|| EntryType.RESERVE_MEDICATION == p.getEntryType()
						|| EntryType.SYMPTOMATIC_MEDICATION == p.getEntryType()))
				.collect(Collectors.toList());
	}

}
