package info.elexis.server.core.connector.elexis.services;

import java.util.List;

import ch.elexis.core.model.Identifiable;

public class PersistenceService2 {

	private static ThreadLocal<String> threadLocalUserId = new ThreadLocal<>();

	/**
	 * Return all elements of a given type
	 * 
	 * @param includeElementsMarkedDeleted
	 *            if <code>true</code> include elements marked as deleted
	 * @return
	 */
	public static List<? extends Identifiable> findAll(Class<? extends Identifiable> clazz,
			boolean includeElementsMarkedDeleted) {
//		EntityManager em = ElexisEntityManager.createEntityManager();
//		try {
//			CriteriaBuilder qb = em.getCriteriaBuilder();
//			CriteriaQuery<? extends AbstractDBObjectIdDeleted> c = qb.createQuery(clazz);
//		
//			if (!includeElementsMarkedDeleted) {
//				Root<? extends AbstractDBObjectIdDeleted> r = c.from(clazz);
//				Predicate delPred = qb.equal(r.get(AbstractDBObjectIdDeleted_.deleted), false);
//				c = c.where(delPred);
//			}
//		
//			TypedQuery<? extends AbstractDBObjectIdDeleted> q = em.createQuery(c);
//			q.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);
//			q.setHint(QueryHints.REFRESH, HintValues.TRUE);
//			return q.getResultList();
//		} finally {
//			em.close();
//		}
		return null;
	}

	public static void setThreadLocalUserId(String userId) {
		PersistenceService2.threadLocalUserId.set(userId);
	};

}
