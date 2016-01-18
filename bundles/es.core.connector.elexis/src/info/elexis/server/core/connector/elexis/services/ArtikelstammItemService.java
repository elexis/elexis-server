package info.elexis.server.core.connector.elexis.services;

import java.util.Date;
import java.util.List;

import at.medevit.ch.artikelstamm.ArtikelstammConstants.TYPE;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;

public class ArtikelstammItemService extends AbstractService<ArtikelstammItem> {

	public static ArtikelstammItemService INSTANCE = InstanceHolder.INSTANCE;
	
	private static final class InstanceHolder {
		static final ArtikelstammItemService INSTANCE = new ArtikelstammItemService();
	}

	private ArtikelstammItemService() {
		super(ArtikelstammItem.class);
	}
	
	private static final String VERSION_ENTRY_ID = "VERSION";
	
	/**
	 * @param stammType
	 * @return The version of the resp {@link TYPE}, or 99999 if not found or error
	 */
	public int getImportSetCumulatedVersion(TYPE stammType) {
		ArtikelstammItem ai = findById(VERSION_ENTRY_ID);
		switch (stammType) {
		case N:
			return Integer.parseInt(ai.getPexf());
		case P:
			return Integer.parseInt(ai.getPpub());
		}
		return 99999;
	}

	public void setImportSetCumulatedVersion(TYPE importStammType, Integer version) {
		ArtikelstammItem ai = findById(VERSION_ENTRY_ID);
		switch (importStammType) {
		case N:
			ai.setPexf(Integer.toString(version));
			return;
		case P:
			ai.setPpub(Integer.toString(version));
			return;
		}
	}

	public static void setImportSetDataQuality(TYPE importStammType, Integer dataquality) {
		// TODO Auto-generated method stub
		
	}

	public static void setImportSetCreationDate(TYPE importStammType, Date time) {
		// TODO Auto-generated method stub
		
	}

	public static List<ArtikelstammItem> getAllArticlesOnStock() {
		// TODO Auto-generated method stub
		return null;
	}
}
