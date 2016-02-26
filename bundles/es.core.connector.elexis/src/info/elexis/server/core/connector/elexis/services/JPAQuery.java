package info.elexis.server.core.connector.elexis.services;

import static info.elexis.server.core.connector.elexis.internal.ElexisEntityManager.createEntityManager;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.eclipse.persistence.jpa.JpaQuery;

import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObject;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted_;

/**
 * This class tries to resemble the Query class known by Elexis user by
 * employing JPA CriteriaQueries.
 * 
 * 
 * @param <T>
 */
public class JPAQuery<T extends AbstractDBObject> {

	public static enum QUERY {
		LIKE, EQUALS, LESS_OR_EQUAL, GREATER
	};

	private CriteriaBuilder cb;
	private CriteriaQuery<T> cq;
	private Root<T> root;
	private TypedQuery<T> query;

	private Predicate predicate;

	private boolean includeDeleted;

	public JPAQuery(Class<T> clazz) {
		this(clazz, false);
	}

	public JPAQuery(Class<T> clazz, boolean includeDeleted) {
		cb = ElexisEntityManager.getCriteriaBuilder();
		cq = cb.createQuery(clazz);
		root = cq.from(clazz);
		this.includeDeleted = includeDeleted;
	}

	public void add(@SuppressWarnings("rawtypes") SingularAttribute attribute, QUERY qt, Object object) {
		Predicate predIn = derivePredicate(attribute, qt, object);

		if (predicate == null) {
			predicate = predIn;
		} else {
			predicate = cb.and(predicate, predIn);
		}
	}

	public void add(SingularAttribute<?, Boolean> attribute, QUERY qt, boolean bool) {
		Predicate predIn = derivePredicate(attribute, qt, bool);

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
			Object value) {
		switch (qt) {
		case LIKE:
			return cb.like(root.get(attribute), value.toString());
		case EQUALS:
			return cb.equal(root.get(attribute), value);
		case LESS_OR_EQUAL:
			Path<Integer> pathLE = root.get(attribute);
			return cb.le(pathLE, Integer.parseInt(value.toString()));
		case GREATER:
			Path<Integer> pathG = root.get(attribute);
			return cb.gt(pathG, Integer.parseInt(value.toString()));
		default:
			throw new IllegalArgumentException();
		}
	}

	public List<T> execute() {
		cq = cq.where(predicate);
		if (!includeDeleted) {
			add(AbstractDBObjectIdDeleted_.id, QUERY.EQUALS, "0");
		}
		query = createEntityManager().createQuery(cq);
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

	public T executeGetSingleResult() {
		List<T> result = execute();
		if (result.size() == 1) {
			return result.get(0);
		} else if (result.size() == 0) {
			throw new NoResultException();
		} else {
			throw new NonUniqueResultException();
		}
	}

}
