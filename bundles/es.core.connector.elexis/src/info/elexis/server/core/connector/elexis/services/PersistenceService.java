package info.elexis.server.core.connector.elexis.services;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted_;

public class PersistenceService {

	/**
	 * Return all elements of a given type
	 * 
	 * @param includeElementsMarkedDeleted
	 *            if <code>true</code> include elements marked as deleted
	 * @return
	 */
	public static List<? extends AbstractDBObjectIdDeleted> findAll(Class<? extends AbstractDBObjectIdDeleted> clazz,
			boolean includeElementsMarkedDeleted) {
		EntityManager em = ElexisEntityManager.createEntityManager();
		try {
			CriteriaBuilder qb = em.getCriteriaBuilder();
			CriteriaQuery<? extends AbstractDBObjectIdDeleted> c = qb.createQuery(clazz);
		
			if (!includeElementsMarkedDeleted) {
				Root<? extends AbstractDBObjectIdDeleted> r = c.from(clazz);
				Predicate delPred = qb.equal(r.get(AbstractDBObjectIdDeleted_.deleted), false);
				c = c.where(delPred);
			}
		
			TypedQuery<? extends AbstractDBObjectIdDeleted> q = em.createQuery(c);
			q.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);
			q.setHint(QueryHints.REFRESH, HintValues.TRUE);
			return q.getResultList();
		} finally {
			em.close();
		}
	}

	/**
	 * Loads an entity from database in detached state.
	 * 
	 * @param clazz
	 * @param id
	 * @return castable to clazz if present
	 */
	public static Optional<AbstractDBObjectIdDeleted> load(Class<? extends AbstractDBObjectIdDeleted> clazz,
			String id) {
		EntityManager em = ElexisEntityManager.createEntityManager();
		try {
			return Optional.ofNullable(em.find(clazz, id));
		} finally {
			em.close();
		}
	}
	
	public static void delete(AbstractDBObjectIdDeleted object) {
		EntityManager em = ElexisEntityManager.createEntityManager();
		try {
			em.getTransaction().begin();
			object = em.merge(object);
			object.setDeleted(true);
			em.getTransaction().commit();
		} finally {
			em.close();
		}
	}

	public static void remove(AbstractDBObjectIdDeleted object) {
		EntityManager em = ElexisEntityManager.createEntityManager();
		try {
			em.getTransaction().begin();
			object = em.merge(object);
			em.remove(object);
			em.getTransaction().commit();
		} finally {
			em.close();
		}
	}

	/**
	 * Saves the entity (create or overwrite) in the database.
	 * 
	 * @param entity
	 */
	public static AbstractDBObjectIdDeleted save(AbstractDBObjectIdDeleted entity) {
		EntityManager em = ElexisEntityManager.createEntityManager();
		try {
			em.getTransaction().begin();
			entity = em.merge(entity);
			em.getTransaction().commit();
			return entity;
		} finally {
			em.close();
		}
	};

}
