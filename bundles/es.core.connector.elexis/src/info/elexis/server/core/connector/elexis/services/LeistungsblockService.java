package info.elexis.server.core.connector.elexis.services;

import java.util.Optional;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Leistungsblock;

public class LeistungsblockService extends PersistenceService {

	/**
	 * convenience method
	 * 
	 * @param id
	 * @return
	 */
	public static Optional<Leistungsblock> load(String id) {
		return PersistenceService.load(Leistungsblock.class, id).map(v -> (Leistungsblock) v);
	}

}
