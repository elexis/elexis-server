package info.elexis.server.core.connector.elexis.services;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Artikel;

public class ArtikelService extends AbstractService<Artikel> {

	public static ArtikelService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final ArtikelService INSTANCE = new ArtikelService();
	}

	private ArtikelService() {
		super(Artikel.class);
	}
}
