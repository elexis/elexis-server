package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDate;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

public class BehandlungService extends AbstractService<Behandlung> {

	public static BehandlungService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final BehandlungService INSTANCE = new BehandlungService();
	}

	private BehandlungService() {
		super(Behandlung.class);
	}

	/**
	 * Create a {@link Behandlung} with mandatory attributes
	 * @param fall
	 * @param mandator
	 * @return
	 */
	public Behandlung create(Fall fall, Kontakt mandator) {
		Behandlung cons = create();
		cons.setDatum(LocalDate.now());
		cons.setFall(fall);
		cons.setMandant(mandator);
//		fall.getPatient().setInfoElement("LetzteBehandlung", getId());
		flush();
		return cons;
	}
}
