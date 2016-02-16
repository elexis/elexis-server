package info.elexis.server.core.connector.elexis.services;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.ElexisTypeMap;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObject;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Xid;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Xid_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.types.XidQuality;

public abstract class AbstractService<T extends AbstractDBObjectIdDeleted> {

	private final Class<T> clazz;

	private static Logger log = LoggerFactory.getLogger(AbstractService.class);

	private EntityTransaction transaction;

	protected EntityManager em;
	protected CriteriaBuilder cb;

	public AbstractService(Class<T> clazz) {
		this.clazz = clazz;
		
		em = ElexisEntityManager.createEntityManager();
		cb = em.getCriteriaBuilder();
	}

	/**
	 * Sychronous operation. Element is directly persisted, leading to id creation.
	 * @return
	 */
	public T create() {
		return create(null, true);
	}

	/**
	 * 
	 * @param performCommit
	 *            whether to perform a commit within the operation
	 * @return an instance of T, or <code>null</code> on any error
	 */
	public T create(final boolean performCommit) {
		return create(null, performCommit);
	}

	/**
	 * 
	 * @param id
	 * @param performCommit
	 * @return
	 */
	public T create(String id, final boolean performCommit) {
		try {
			T obj = clazz.newInstance();
			obj.setDeleted(false);
			if (performCommit)
				em.getTransaction().begin();
			if (id != null) {
				obj.setId(id);
			}
			em.persist(obj);
			if (performCommit)
				em.getTransaction().commit();
			return obj;
		} catch (IllegalAccessException | InstantiationException e) {
			log.error("Error creating instance " + clazz.getName(), e);
			return null;
		}
	}

	/**
	 * Create a transaction and flush all open changes onto the database
	 */
	public void flush() {
		em.getTransaction().begin();
		em.flush();
		em.getTransaction().commit();
	}

	/**
	 * Find an object by its primary id.
	 * 
	 * @param id
	 * @param entityClass
	 * @return
	 */
	public T findById(Object id) {
		return (T) em.find(clazz, id);
	}

	/**
	 * Returns a list of elements according to fuzzy ID starts with matching
	 * 
	 * @param string
	 * @param entityClass
	 * @return
	 */
	public List<T> findByIdStartingWith(String string) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> c = cb.createQuery(clazz);
		Root<T> r = c.from(clazz);
		Predicate like = cb.like(r.get("id"), string + "%");
		c = c.where(like);
		TypedQuery<T> q = em.createQuery(c);
		return q.getResultList();
	}

	/**
	 * Return all elements of a given type
	 * 
	 * @param includeElementsMarkedDeleted
	 *            if <code>true</code> include elements marked as deleted
	 * @return
	 */
	public List<T> findAll(boolean includeElementsMarkedDeleted) {
		CriteriaBuilder qb = em.getCriteriaBuilder();
		CriteriaQuery<T> c = qb.createQuery(clazz);

		if (!includeElementsMarkedDeleted) {
			Root<T> r = c.from(clazz);
			Predicate delPred = qb.equal(r.get(AbstractDBObjectIdDeleted_.deleted), false);
			c = c.where(delPred);
		}

		TypedQuery<T> q = em.createQuery(c);
		q.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);
		return q.getResultList();
	};

	/**
	 * @param object
	 * @param domain
	 * @return the ID for a given object in the domain as stored by the
	 *         {@link Xid} or <code>null</code>, if not found
	 */
	public String getDomainId(T object, String domain) {
		JPAQuery<Xid> qre = new JPAQuery<Xid>(Xid.class);
		qre.add(Xid_.domain, JPAQuery.QUERY.LIKE, domain);
		qre.add(Xid_.object, JPAQuery.QUERY.LIKE, object.getId());
		// TODO add type as criteria?
		List<Xid> results = qre.execute();

		if (results.size() == 1) {
			return results.get(0).getDomainId();
		}
		if (results.size() == 0) {
			return null;
		}

		log.warn("Multiple domainId entries for {} in domain {} found.", object.getId(), domain);
		return null;
	}

	/**
	 * Set or create an {@link Xid} for the provided values. If an existing entry is found,
	 * the domainId value will be overwritten.
	 * @param obj
	 * @param domain
	 * @param domainId
	 * @param quality
	 * @return <code>null</code> on error
	 */
	public Xid setDomainId(T obj, String domain, String domainId, XidQuality quality) {
		JPAQuery<Xid> qre = new JPAQuery<Xid>(Xid.class);
		qre.add(Xid_.domain, JPAQuery.QUERY.LIKE, domain);
		qre.add(Xid_.object, JPAQuery.QUERY.LIKE, obj.getId());
		qre.add(Xid_.type, JPAQuery.QUERY.LIKE, ElexisTypeMap.getKeyForObject(obj));
		List<Xid> result = qre.execute();
		if (result.size() == 0) {
			return XidService.INSTANCE.create(domain, domainId, obj, quality);
		} else if (result.size() == 1) {
			Xid xid = result.get(0);
			xid.setDomainId(domainId);
			return xid;
		}
		log.error("Multiple XID entries for {}, {}", domain, obj.getId());
		return null;
	}

	/**
	 * Removes an entity from the database. <b>WARNING</b> this call effectively
	 * removes the entry, to mark it as deleted use {@link #delete(Object)}
	 */
	public void remove(T entity) {
		remove(entity, true);
	}

	/**
	 * see {@link #remove(AbstractDBObject)}
	 * 
	 * @param entity
	 * @param performCommit
	 *            whether to perform a commit within the operation
	 */
	public void remove(T entity, final boolean performCommit) {
		if (performCommit)
			em.getTransaction().begin();
		em.remove(entity);
		if (performCommit)
			em.getTransaction().commit();
	}

	/**
	 * Mark the entity as deleted, performs as transaction
	 * 
	 * @param entity
	 */
	public void delete(T entity) {
		em.getTransaction().begin();
		entity.setDeleted(true);
		em.getTransaction().commit();
	}

	public void beginTransaction() {
		transaction = em.getTransaction();
		transaction.begin();
	}

	public void commitTransaction() {
		if (transaction == null) {
			throw new IllegalStateException("transaction is null, execute #beginTransaction() first");
		}
		transaction.commit();
		transaction = null;
	}
}
