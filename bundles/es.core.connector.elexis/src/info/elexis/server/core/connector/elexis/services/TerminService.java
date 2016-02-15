package info.elexis.server.core.connector.elexis.services;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Termin;

public class TerminService extends AbstractService<Termin>{
	public static TerminService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final TerminService INSTANCE = new TerminService();
	}

	private TerminService() {
		super(Termin.class);
	}
	
	
}
