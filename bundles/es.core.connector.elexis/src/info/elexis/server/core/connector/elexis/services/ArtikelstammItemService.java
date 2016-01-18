package info.elexis.server.core.connector.elexis.services;

import at.medevit.ch.artikelstamm.ArtikelstammConstants.TYPE;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;

public class ArtikelstammItemService extends AbstractService<ArtikelstammItem> {

	private static final String VERSION_ENTRY_ID = "VERSION";

	public ArtikelstammItemService() {
		super(ArtikelstammItem.class);
	}

	/**
	 * @param stammType
	 * @return The version of the resp {@link TYPE}, or 99999 if not found or error
	 */
	public static int getImportSetCumulatedVersion(TYPE stammType) {
		ArtikelstammItem version = findById(VERSION_ENTRY_ID, ArtikelstammItem.class);
		switch (stammType) {
		case N:
			return Integer.parseInt(version.getPexf());
		case P:
			return Integer.parseInt(version.getPpub());
		}
		return 99999;
	}
}
