package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDate;
import java.util.List;

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
		pres.setDateFrom(LocalDate.now());
		
		em.getTransaction().commit();
		
		return pres;
	}
	
	/**
	 * Find all prescriptions for patient not deleted, and not defined recipe field
	 * @param patient
	 * @return
	 */
	public static List<Prescription> findAllNonDeletedPrescriptionsForPatient(Kontakt patient) {
		JPAQuery<Prescription> qbe = new JPAQuery<Prescription>(Prescription.class);
		qbe.add(Prescription_.patient, JPAQuery.QUERY.EQUALS, patient);
		qbe.add(Prescription_.rezeptID, JPAQuery.QUERY.EQUALS, null);
		return qbe.execute();
	}

}
