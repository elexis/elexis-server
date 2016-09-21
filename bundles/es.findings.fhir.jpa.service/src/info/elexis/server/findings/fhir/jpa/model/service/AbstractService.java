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

import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Xid;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.types.XidQuality;

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
	 * @param object
	 * @param domain
	 * @return the ID for a given object in the domain as stored by the
	 *         {@link Xid} or <code>null</code>, if not found
	 */
	public String getDomainId(T object, String domain) {
		// JPAQuery<Xid> qre = new JPAQuery<Xid>(Xid.class);
		// qre.add(Xid_.domain, JPAQuery.QUERY.LIKE, domain);
		// qre.add(Xid_.object, JPAQuery.QUERY.LIKE, object.getId());
		// // TODO add type as criteria?
		// List<Xid> results = qre.execute();
		//
		// if (results.size() == 1) {
		// return results.get(0).getDomainId();
		// }
		// if (results.size() == 0) {
		// return null;
		// }
		//
		// log.warn("Multiple domainId entries for {} in domain {} found.",
		// object.getId(), domain);
		// return null;
		return null;
	}

	/**
	 * Set or create an {@link Xid} for the provided values. If an existing
	 * entry is found, the domainId value will be overwritten.
	 * 
	 * @param obj
	 * @param domain
	 * @param domainId
	 * @param quality
	 * @return <code>null</code> on error
	 */
	public Xid setDomainId(T obj, String domain, String domainId, XidQuality quality) {
		// JPAQuery<Xid> qre = new JPAQuery<Xid>(Xid.class);
		// qre.add(Xid_.domain, JPAQuery.QUERY.LIKE, domain);
		// qre.add(Xid_.object, JPAQuery.QUERY.LIKE, obj.getId());
		// List<Xid> result = qre.execute();
		// if (result.size() == 0) {
		// XidService.INSTANCE.create(domain, domainId, obj, quality);
		// em.refresh(obj);
		// } else if (result.size() == 1) {
		// Xid xid = result.get(0);
		// xid.setDomainId(domainId);
		// XidService.INSTANCE.write(xid);
		// return xid;
		// }
		// log.error("Multiple XID entries for {}, {}", domain, obj.getId());
		return null;
	}

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
