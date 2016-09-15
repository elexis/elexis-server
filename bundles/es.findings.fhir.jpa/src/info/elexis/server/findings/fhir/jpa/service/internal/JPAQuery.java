package info.elexis.server.findings.fhir.jpa.service.internal;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.eclipse.persistence.jpa.JpaQuery;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObject;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
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
		LIKE, EQUALS, LESS_OR_EQUAL, GREATER, NOT_LIKE, NOT_EQUALS, GREATER_OR_EQUAL
	};

	private EntityManager em;
	private CriteriaBuilder cb;
	private CriteriaQuery<T> cq;
	private Root<T> root;
	private TypedQuery<T> query;

	private Predicate predicate;
	private boolean includeDeleted;

	private final Class<T> clazz;

	public JPAQuery(Class<T> clazz) {
		this(clazz, false);
	}

	public JPAQuery(Class<T> clazz, boolean includeDeleted) {
		em = FindingsEntityManager.getEntityManager();
		cb = em.getCriteriaBuilder();
		cq = cb.createQuery(clazz);
		root = cq.from(clazz);
		this.clazz = clazz;
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

	public void addLikeNormalized(@SuppressWarnings("rawtypes") SingularAttribute attribute, String value) {
		Predicate predIn = cb.like(cb.lower(root.get(attribute)), value.toString().toLowerCase());
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
		case GREATER_OR_EQUAL:
			Path<Integer> pathGE = root.get(attribute);
			return cb.ge(pathGE, Integer.parseInt(value.toString()));
		case NOT_LIKE:
			return cb.not(cb.like(root.get(attribute), value.toString()));
		case NOT_EQUALS:
			return cb.not(cb.equal(root.get(attribute), value));
		default:
			throw new IllegalArgumentException();
		}
	}

	public List<T> execute() {
		if (!includeDeleted) {
			if (AbstractDBObjectIdDeleted.class.isAssignableFrom(clazz)) {
				add(AbstractDBObjectIdDeleted_.deleted, QUERY.EQUALS, false);
			}
		}
		cq = cq.where(predicate);
		query = em.createQuery(cq);
		try {
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	@Override
	public String toString() {
		if (query != null) {
			// will only print SQL string after execute()
			return query.unwrap(JpaQuery.class).getDatabaseQuery().getSQLString();
		}
		return super.toString();
	}

	/**
	 * @return the element if the respective list contains exactly one element
	 */
	public Optional<T> executeGetSingleResult() {
		List<T> result = execute();
		if (result != null && result.size() == 1) {
			return Optional.of(result.get(0));
		}
		return Optional.empty();
	}

}
