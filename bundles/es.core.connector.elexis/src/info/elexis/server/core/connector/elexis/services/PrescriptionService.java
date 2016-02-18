package info.elexis.server.core.connector.elexis.services;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription;

public class PrescriptionService extends AbstractService<Prescription> {

	public static PrescriptionService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final PrescriptionService INSTANCE = new PrescriptionService();
	}

	private PrescriptionService() {
		super(Prescription.class);
	}
}
