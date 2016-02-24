package info.elexis.server.core.connector.elexis.services;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;

public class FallService extends AbstractService<Fall> {

	public static FallService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final FallService INSTANCE = new FallService();
	}

	private FallService() {
		super(Fall.class);
	}


}
