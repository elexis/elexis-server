package info.elexis.server.core.connector.elexis.services;

import static info.elexis.server.core.connector.elexis.internal.ElexisEntityManager.*;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.SingularAttribute;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.eclipse.persistence.queries.ScrollableCursor;
import org.eclipse.persistence.sessions.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	protected static Logger log = LoggerFactory.getLogger(JPAQuery.class);

	public static enum QUERY {
		LIKE, EQUALS, LESS_OR_EQUAL, GREATER, NOT_LIKE, NOT_EQUALS, GREATER_OR_EQUAL
	};

	private ExpressionBuilder emp = new ExpressionBuilder();
	private ReadAllQuery readAllQuery;
	private Expression predicate;

	private boolean includeDeleted;

	private final Class<T> clazz;

	public JPAQuery(Class<T> clazz) {
		this(clazz, false);
	}

	public JPAQuery(Class<T> clazz, boolean includeDeleted) {
		readAllQuery = new ReadAllQuery(clazz);
		this.clazz = clazz;
		this.includeDeleted = includeDeleted;
	}

	public void add(@SuppressWarnings("rawtypes") SingularAttribute attribute, QUERY qt, Object object) {
		Expression predIn = derivePredicate(attribute, qt, object);

		if (predicate == null) {
			predicate = predIn;
		} else {
			predicate = predicate.and(predIn);
		}
	}

	public void add(SingularAttribute<?, Boolean> attribute, QUERY qt, boolean bool) {
		Expression predIn = derivePredicate(attribute, qt, bool);

		if (predicate == null) {
			predicate = predIn;
		} else {
			predicate = predicate.and(predIn);
		}
	}

	public void addLikeNormalized(@SuppressWarnings("rawtypes") SingularAttribute attribute, String value) {
		Expression predIn = emp.get(attribute.getName()).likeIgnoreCase(value.toString());
		if (predicate == null) {
			predicate = predIn;
		} else {
			predicate = predicate.and(predIn);
		}
	}

	public void or(@SuppressWarnings("rawtypes") SingularAttribute attribute, QUERY qt, String string) {
		Expression predIn = derivePredicate(attribute, qt, string);

		if (predicate == null) {
			predicate = predIn;
		} else {
			predicate = predicate.and(predIn);
		}
	}

	private Expression derivePredicate(@SuppressWarnings("rawtypes") SingularAttribute attribute, QUERY qt,
			Object value) {
		switch (qt) {
		case LIKE:
			return emp.get(attribute.getName()).like(value.toString());
		case EQUALS:
			return emp.get(attribute.getName()).equal(value);
		case LESS_OR_EQUAL:
			return emp.get(attribute.getName()).lessThanEqual(Integer.parseInt(value.toString()));
		case GREATER:
			return emp.get(attribute.getName()).greaterThan(Integer.parseInt(value.toString()));
		case GREATER_OR_EQUAL:
			return emp.get(attribute.getName()).greaterThanEqual(Integer.parseInt(value.toString()));
		case NOT_LIKE:
			return emp.not().get(attribute.getName()).like(value.toString());
		case NOT_EQUALS:
			return emp.not().get(attribute.getName()).equal(value);
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

		if (predicate != null) {
			readAllQuery.setSelectionCriteria(predicate);
		}

		EntityManager entityManager = createEntityManager();
		Session session = ((org.eclipse.persistence.jpa.JpaEntityManager) entityManager.getDelegate())
				.getActiveSession();
		try {
			return (List) session.executeQuery(readAllQuery);
		} finally {
			entityManager.close();
		}
	}

	public ScrollableCursor executeAsStream() {
		if (!includeDeleted) {
			if (AbstractDBObjectIdDeleted.class.isAssignableFrom(clazz)) {
				add(AbstractDBObjectIdDeleted_.deleted, QUERY.EQUALS, false);
			}
		}

		if (predicate != null) {
			readAllQuery.setSelectionCriteria(predicate);
		}

		EntityManager entityManager = createEntityManager();
		readAllQuery.useScrollableCursor();
		Session session = ((org.eclipse.persistence.jpa.JpaEntityManager) entityManager.getDelegate())
				.getActiveSession();
		ScrollableCursor cursor = null;
		try {
			cursor = (ScrollableCursor) session.executeQuery(readAllQuery);
			return cursor;
		} finally {
			entityManager.close();
		}
	}

	/**
	 * @return the element if the respective list contains exactly one element
	 */
	public Optional<T> executeGetSingleResult() {
		List<T> result = execute();
		if (result != null) {
			if (result.size() > 1) {
				log.warn("executeGetSingleResult() returned {} results", result.size());
			}
			if (result.size() == 1) {
				return Optional.of(result.get(0));
			}
		}
		return Optional.empty();
	}

}
