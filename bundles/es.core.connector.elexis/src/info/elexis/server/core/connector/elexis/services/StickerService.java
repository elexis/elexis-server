//package info.elexis.server.core.connector.elexis.services;
//
//import java.util.Iterator;
//import java.util.List;
//import java.util.Optional;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import javax.persistence.EntityManager;
//import javax.persistence.Query;
//
//import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
//import info.elexis.server.core.connector.elexis.jpa.ElexisTypeMap;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Sticker;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.StickerClassLink;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.StickerObjectLink;
//
//public class StickerService extends PersistenceService {
//
//	/**
//	 * Find all stickers applicable to a certain class.
//	 * 
//	 * @param clazz
//	 *            as used in {@link Sticker#setStickerClassLinks(List)}. Please
//	 *            refer to {@link ElexisTypeMap} for relevant strings
//	 * @return
//	 */
//	@SuppressWarnings("unchecked")
//	public static List<Sticker> findStickersApplicableToClass(String clazz) {
//		EntityManager em = ElexisEntityManager.createEntityManager();
//		try {
//			String jpql = "SELECT s FROM Sticker s, IN (s.stickerClassLinks) scl WHERE scl.objclass = :objclass";
//			Query query = em.createQuery(jpql);
//			query.setParameter("objclass", clazz);
//			return query.getResultList();
//		} finally {
//			em.close();
//		}
//	}
//
//	/**
//	 * Determine whether a Sticker is applicable to the given class
//	 * 
//	 * @param sticker
//	 * @param clazz
//	 * @return
//	 */
//	public static boolean isStickerApplicableToClass(Sticker sticker, String clazz) {
//		EntityManager em = ElexisEntityManager.createEntityManager();
//		try {
//			String jpql = "SELECT COUNT(s) FROM Sticker s, IN (s.stickerClassLinks) scl WHERE scl.objclass = :objclass AND s.id = :stickerId";
//			Query query = em.createQuery(jpql);
//			query.setParameter("objclass", clazz);
//			query.setParameter("stickerId", sticker.getId());
//			long result = (long) query.getSingleResult();
//			return (result == 1);
//		} finally {
//			em.close();
//		}
//	}
//
//	/**
//	 * Returns all Stickers mounted on a given object
//	 * 
//	 * @param object
//	 * @return
//	 */
//	@SuppressWarnings("unchecked")
//	public static List<Sticker> findStickersOnObject(AbstractDBObjectIdDeleted object) {
//		EntityManager em = ElexisEntityManager.createEntityManager();
//		try {
//			String jpql = "SELECT s FROM Sticker s, IN (s.stickerObjectLinks) sol WHERE sol.obj = :obj";
//			Query query = em.createQuery(jpql);
//			query.setParameter("obj", object.getId());
//			return query.getResultList();
//		} finally {
//			em.close();
//		}
//	}
//
//	/**
//	 * Tries to apply a sticker to a given object.
//	 * 
//	 * @param sticker
//	 * @param object
//	 * @return <code>false</code> if the {@link Sticker} could not be applied
//	 */
//	public static boolean applyStickerToObject(Sticker sticker, AbstractDBObjectIdDeleted object) {
//		boolean isApplicable = isStickerApplicableToClass(sticker, ElexisTypeMap.getKeyForObject(object));
//		if (isApplicable) {
//			StickerObjectLink sol = new StickerObjectLink();
//			sol.setSticker(sticker);
//			sol.setObj(object.getId());
//			sticker.getStickerObjectLinks().add(sol);
//			StickerService.save(sticker);
//			return true;
//		}
//		return false;
//	}
//
//	/**
//	 * Remove all stickers from object
//	 * 
//	 * @param object
//	 */
//	public static void removeAllStickersFromObject(AbstractDBObjectIdDeleted object) {
//		List<Sticker> stickers = StickerService.findStickersOnObject(object);
//		for (Sticker sticker : stickers) {
//			StickerService.removeStickerFromObject(sticker, object);
//		}
//	}
//
//	/**
//	 * Removes the sticker from object if possible
//	 * 
//	 * @param sticker
//	 * @param object
//	 */
//	public static void removeStickerFromObject(Sticker sticker, AbstractDBObjectIdDeleted object) {
//		Set<StickerObjectLink> stickerObjectLinks = sticker.getStickerObjectLinks();
//		for (Iterator<StickerObjectLink> iterator = stickerObjectLinks.iterator(); iterator.hasNext();) {
//			StickerObjectLink sol = (StickerObjectLink) iterator.next();
//			if (sol.getObj().equals(object.getId())) {
//				iterator.remove();
//			}
//		}
//		StickerService.save(sticker);
//	}
//}
