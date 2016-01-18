package info.elexis.server.core.connector.elexis.services;

import static info.elexis.server.core.connector.elexis.internal.ElexisEntityManager.em;

import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObject;

public class AbstractService<T extends AbstractDBObject> {

	private final Class<T> clazz;

	public AbstractService(Class<T> clazz) {
		this.clazz = clazz;
	}

	/**
	 * Find an object by its primary id.
	 * @param id
	 * @param entityClass
	 * @return
	 */
	public T findById(Object id) {
		return (T) em().find(clazz, id);
	}
	
	/**
	 * Returns a list of elements according to fuzzy ID starts with matching
	 * @param string
	 * @param entityClass
	 * @return
	 */
	public List<T> findByIdStartingWith(String string) {
		CriteriaBuilder qb = em().getCriteriaBuilder();
		CriteriaQuery<T> c = qb.createQuery(clazz);
		Root<T> r = c.from(clazz);
		Predicate like = qb.like(r.get("id"), string+"%");
		c = c.where(like);
		TypedQuery<T> q = em().createQuery(c);
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
		CriteriaBuilder qb = em().getCriteriaBuilder();
		CriteriaQuery<T> c = qb.createQuery(clazz);

		if (!includeElementsMarkedDeleted) {
			Root<T> r = c.from(clazz);
			Predicate like = qb.like(r.get("deleted"), "0");
			c = c.where(like);
		}

		TypedQuery<T> q = em().createQuery(c);
		return q.getResultList();
	};
	
	/**
	 * Removes an entity from the database. <b>WARNING</b> this call effectively
	 * removes the entry, to mark it as deleted use {@link #delete(Object)}
	 */
	public void remove(T entity) {
		em().remove(entity);
	}
	
	/**
	 * Mark the entity as deleted
	 * @param entity
	 */
	public void delete(T entity) {
		entity.setDeleted(true);
	}
}
