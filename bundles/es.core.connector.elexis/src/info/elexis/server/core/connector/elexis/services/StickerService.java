package info.elexis.server.core.connector.elexis.services;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.ElexisTypeMap;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Sticker;

public class StickerService extends PersistenceService {
	/**
	 * Loads a {@link Sticker} by id
	 * 
	 * @param id
	 * @return
	 */
	public static Optional<Sticker> load(String id) {
		return PersistenceService.load(Sticker.class, id).map(v -> (Sticker) v);
	}

	/**
	 * Saves a {@link Sticker}
	 * 
	 * @param sticker
	 * @return
	 */
	public static Sticker save(Sticker sticker) {
		return (Sticker) PersistenceService.save(sticker);
	}

	/**
	 * Find all stickers applicable to a certain class.
	 * 
	 * @param clazz
	 *            as used in {@link Sticker#setStickerClassLinks(List)}. Please
	 *            refer to {@link ElexisTypeMap} for relevant strings
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Sticker> findStickersApplicableToClass(String clazz) {
		EntityManager em = ElexisEntityManager.createEntityManager();
		try {
			String jpql = "SELECT s FROM Sticker s, IN (s.stickerClassLinks) scl WHERE scl.objclass = :objclass";
			Query query = em.createQuery(jpql);
			query.setParameter("objclass", clazz);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
}
