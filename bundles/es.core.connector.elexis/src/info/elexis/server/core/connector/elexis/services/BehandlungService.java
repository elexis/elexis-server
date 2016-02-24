package info.elexis.server.core.connector.elexis.services;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;

public class BehandlungService extends AbstractService<Behandlung> {

	public static BehandlungService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final BehandlungService INSTANCE = new BehandlungService();
	}

	private BehandlungService() {
		super(Behandlung.class);
	}
}
