package info.elexis.server.core.connector.elexis.services;

import java.util.List;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted_;
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
	
	public List<Prescription> findAllNonDeletedPrescriptionsForPatient(Kontakt patient) {
		JPAQuery<Prescription> qbe = new JPAQuery<Prescription>(Prescription.class);
		qbe.add(Prescription_.patient, JPAQuery.QUERY.EQUALS, patient);
		qbe.add(Prescription_.rezeptID, JPAQuery.QUERY.EQUALS, null);
		qbe.add(AbstractDBObjectIdDeleted_.deleted, JPAQuery.QUERY.EQUALS, false);
		return qbe.execute();
	}
}
