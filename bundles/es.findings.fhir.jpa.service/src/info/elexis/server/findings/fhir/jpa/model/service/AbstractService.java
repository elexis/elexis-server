package info.elexis.server.findings.fhir.jpa.model.service;

import java.util.Collections;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.findings.fhir.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.findings.fhir.jpa.model.annotated.AbstractDBObjectIdDeleted_;

public abstract class AbstractService<T extends AbstractDBObjectIdDeleted> {

	private final Class<T> clazz;

	protected static Logger log = LoggerFactory.getLogger(AbstractService.class);

	public AbstractService(Class<T> clazz) {
		this.clazz = clazz;
	}

	protected abstract EntityManager getEntityManager();
	
	/**
	 * Sychronous operation. Element is directly persisted, leading to id
	 * creation.
	 * 
	 * @return
	 */
	public T create() {
		EntityManager em = getEntityManager();
		try {
			T obj = clazz.newInstance();
			obj.setDeleted(false);
			em.getTransaction().begin();
			em.persist(obj);
			em.getTransaction().commit();
			return obj;
		} catch (IllegalAccessException | InstantiationException e) {
			log.error("Error creating instance " + clazz.getName(), e);
			return null;
		} finally {
			em.close();
		}
	}

	/**
	 * Find an object by its primary id.
	 * 
	 * @param id
	 * @param entityClass
	 * @return
	 */
	public Optional<T> findById(Object id) {
		EntityManager em = getEntityManager();
		try {
			if (id == null) {
				log.warn("null provided as argument to findById(Object id)", new Throwable());
				return Optional.empty();
			}
			return Optional.ofNullable((T) em.find(clazz, id));
		} finally {
			em.close();
		}
	}

	/**
	 * Refresh the object from the database
	 * 
	 * @param object
	 */
	public void refresh(T object) {
		EntityManager em = getEntityManager();
		try {
			if (object == null) {
				return;
			}
			if (em.contains(object)) {
				em.refresh(object);
			}
		} finally {
			em.close();
		}
	}

	/**
	 * Returns a list of elements according to fuzzy ID starts with matching.
	 * Useful with deterministic Id strings.
	 * 
	 * @param string
	 * @param entityClass
	 * @return
	 */
	public List<T> findByIdStartingWith(String string) {
		EntityManager em = getEntityManager();
		try {
			if (string == null) {
				log.warn("null provided as argument to findByIdStartingWith(String string)");
				return Collections.emptyList();
			}

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<T> c = cb.createQuery(clazz);
			Root<T> r = c.from(clazz);
			Predicate like = cb.like(r.get("id"), string + "%");
			c = c.where(like);
			TypedQuery<T> q = em.createQuery(c);
			return q.getResultList();
		} finally {
			em.close();
		}
	}

	/**
	 * Return all elements of a given type
	 * 
	 * @param includeElementsMarkedDeleted
	 *            if <code>true</code> include elements marked as deleted
	 * @return
	 */
	public List<T> findAll(boolean includeElementsMarkedDeleted) {
		EntityManager em = getEntityManager();
		try {
			CriteriaBuilder qb = em.getCriteriaBuilder();
			CriteriaQuery<T> c = qb.createQuery(clazz);

			if (!includeElementsMarkedDeleted) {
				Root<T> r = c.from(clazz);
				Predicate delPred = qb.equal(r.get(AbstractDBObjectIdDeleted_.deleted), false);
				c = c.where(delPred);
			}

			TypedQuery<T> q = em.createQuery(c);
			q.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);
			q.setHint(QueryHints.REFRESH, HintValues.TRUE);
			return q.getResultList();
		} finally {
			em.close();
		}
	};

	/**
	 * Write detached entity to database. Find methods return detached objects,
	 * use this method to write changes to these objects.
	 * 
	 * @param entity
	 */
	public T write(T entity) {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.merge(entity);
			em.getTransaction().commit();
			return entity;
		} finally {
			em.close();
		}
	}

	/**
	 * Removes an entity from the database. <b>WARNING</b> this call effectively
	 * removes the entry, to mark it as deleted use {@link #delete(Object)}
	 */
	public void remove(T entity) {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.remove(entity);
			em.getTransaction().commit();
		} finally {
			em.close();
		}
	}

	/**
	 * Mark the entity as deleted, performs as transaction
	 * 
	 * @param entity
	 */
	public void delete(T entity) {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			entity.setDeleted(true);
			em.merge(entity);
			em.getTransaction().commit();
		} finally {
			em.close();
		}
	}
}
