package info.elexis.server.core.connector.elexis.services;

import java.util.Optional;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Sticker;

public class StickerService extends PersistenceService {
	/**
	 * Loads a {@link Sticker} by id
	 * 
	 * @param id
	 * @return
	 */
	public static Optional<Sticker> load(String id){
		return PersistenceService.load(Sticker.class, id).map(v -> (Sticker) v);
	}
	
	/**
	 * Saves a {@link Sticker}
	 * @param sticker
	 * @return
	 */
	public static Sticker save(Sticker sticker){
		return (Sticker) PersistenceService.save(sticker);
	}
	
}
