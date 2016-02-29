package info.elexis.server.core.connector.elexis.services;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Labor2009Tarif;

public class Labor2009TarifService extends AbstractService<Labor2009Tarif> {

	public static Labor2009TarifService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final Labor2009TarifService INSTANCE = new Labor2009TarifService();
	}

	private Labor2009TarifService() {
		super(Labor2009Tarif.class);
	}

}
