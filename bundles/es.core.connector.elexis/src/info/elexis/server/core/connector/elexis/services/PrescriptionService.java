package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import ch.elexis.core.model.prescription.EntryType;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription_;

public class PrescriptionService extends AbstractService<Prescription> {

	public static PrescriptionService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final PrescriptionService INSTANCE = new PrescriptionService();
	}

	private PrescriptionService() {
		super(Prescription.class);
	}

	public Prescription create(AbstractDBObjectIdDeleted article, Kontakt patient, String dosage) {
		em.getTransaction().begin();

		Prescription pres = create(false);
		em.merge(article);
		pres.setArtikel(article);
		em.merge(patient);
		pres.setPatient(patient);
		pres.setDosis(dosage);
		pres.setDateFrom(LocalDateTime.now());

		em.getTransaction().commit();

		return pres;
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
		// as a boost, if this is set it always refers to a recipe or dispensation
		qbe.add(Prescription_.rezeptID, JPAQuery.QUERY.EQUALS, null);
		List<Prescription> result = qbe.execute();
		return result.stream()
				.filter(p -> (EntryType.FIXED_MEDICATION == p.getEntryType()
						|| EntryType.RESERVE_MEDICATION == p.getEntryType()
						|| EntryType.SYMPTOMATIC_MEDICATION == p.getEntryType()))
				.collect(Collectors.toList());
	}

}
