package info.elexis.server.core.connector.elexis.services;

import static info.elexis.server.core.connector.elexis.internal.ElexisEntityManager.em;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.eclipse.persistence.jpa.JpaQuery;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObject;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;

/**
 * This class tries to resemble the Query class known by Elexis user by
 * employing JPA CriteriaQueries.
 * 
 * @param <T>
 */
public class JPAQuery<T extends AbstractDBObject> {

	public static enum QUERY {
		LIKE, EQUALS, LESS_OR_EQUAL, GREATER
	};

	private Class<T> clazz;

	private CriteriaBuilder cb = em().getCriteriaBuilder();
	private CriteriaQuery<T> cq;
	private Root<T> root;
	private TypedQuery<T> query;

	private Predicate predicate;

	public JPAQuery(Class<T> clazz) {
		this.clazz = clazz;

		cq = cb.createQuery(clazz);
		root = cq.from(clazz);
		query = null;
	}

	public void add(@SuppressWarnings("rawtypes") SingularAttribute attribute, QUERY qt, String string) {
		Predicate predIn = derivePredicate(attribute, qt, string);

		if (predicate == null) {
			predicate = predIn;
		} else {
			predicate = cb.and(predicate, predIn);
		}
	}
	
	public void or(@SuppressWarnings("rawtypes") SingularAttribute attribute, QUERY qt, String string) {
		Predicate predIn = derivePredicate(attribute, qt, string);

		if (predicate == null) {
			predicate = predIn;
		} else {
			predicate = cb.or(predicate, predIn);
		}
	}

	@SuppressWarnings("unchecked")
	private Predicate derivePredicate(@SuppressWarnings("rawtypes") SingularAttribute attribute, QUERY qt,
			String string) {
		switch (qt) {
		case LIKE:
			return cb.like(root.get(attribute), string);
		case EQUALS:
			return cb.equal(root.get(attribute), string);
		case LESS_OR_EQUAL:
			Path<Integer> pathLE = root.get(attribute);
			return cb.le(pathLE, Integer.parseInt(string));
		case GREATER:
			Path<Integer> pathG = root.get(attribute);
			return cb.gt(pathG, Integer.parseInt(string));
		default:
			throw new IllegalArgumentException();
		}
	}

	public List<T> execute() {
		cq = cq.where(predicate);
		query = em().createQuery(cq);
		return query.getResultList();
	}

	@Override
	public String toString() {
		if (query != null) {
			// will only print SQL string after execute()
			return query.unwrap(JpaQuery.class).getDatabaseQuery().getSQLString();
		}
		return super.toString();
	}

}
