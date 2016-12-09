package info.elexis.server.core.connector.elexis.services;

import static info.elexis.server.core.connector.elexis.internal.ElexisEntityManager.*;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.SingularAttribute;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.queries.ReportQueryResult;
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

	public static enum ORDER {
		ASC, DESC;
	}

	private ExpressionBuilder emp = new ExpressionBuilder();
	private ReadAllQuery readAllQuery;
	private Expression predicate;

	private boolean includeDeleted;

	private final Class<T> clazz;

	public JPAQuery(Class<T> clazz) {
		this(clazz, false);
	}

	public JPAQuery(Class<T> clazz, boolean includeDeleted) {
		this.clazz = clazz;
		this.includeDeleted = includeDeleted;
	}

	private void initializeReadAllQuery() {
		readAllQuery = new ReadAllQuery(clazz);
		readAllQuery.setIsReadOnly(true);
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
			predicate = predicate.or(predIn);
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
			return  emp.get(attribute.getName()).like(value.toString()).not();
		case NOT_EQUALS:
			return emp.get(attribute.getName()).equal(value).not();
		default:
			throw new IllegalArgumentException();
		}
	}

	public long count() {
		ReportQuery rq = new ReportQuery(clazz, emp);
		rq.setIsReadOnly(true);
		rq.addCount();

		if (!includeDeleted) {
			if (AbstractDBObjectIdDeleted.class.isAssignableFrom(clazz)) {
				add(AbstractDBObjectIdDeleted_.deleted, QUERY.EQUALS, false);
			}
		}

		if (predicate != null) {
			rq.setSelectionCriteria(predicate);
		}

		EntityManager entityManager = createEntityManager();
		Session session = ((org.eclipse.persistence.jpa.JpaEntityManager) entityManager.getDelegate())
				.getActiveSession();
		try {
			@SuppressWarnings("rawtypes")
			List reportRows = (List) session.executeQuery(rq);
			if (reportRows.size() > 0) {
				ReportQueryResult value = (ReportQueryResult) reportRows.get(0);
				return new Integer((int) value.getResults().get(0)).longValue();
			}
			return -1;
		} finally {
			entityManager.close();
		}
	}

	public List<T> execute() {
		initializeReadAllQuery();

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

	/**
	 * 
	 * @param orderBy
	 *            order the results according to the given attribute
	 * @param order
	 *            the order direction
	 * @param offset
	 *            the offset to start delivering results from or
	 *            <code>null</code>
	 * @param limit
	 *            the maximum number of results to deliver (starting from
	 *            offset, if applied) or <code>null</code>
	 * @return
	 */
	public ScrollableCursor executeAsStream(@SuppressWarnings("rawtypes") SingularAttribute orderBy, ORDER order,
			Integer offset, Integer limit) {
		initializeReadAllQuery();

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
		readAllQuery.dontMaintainCache();
		if (orderBy != null && order != null) {
			if (ORDER.DESC == order) {
				readAllQuery.addDescendingOrdering(orderBy.getName());
			} else {
				readAllQuery.addAscendingOrdering(orderBy.getName());
			}
		}

		if (offset != null) {
			readAllQuery.setFirstResult(offset);
		}
		if (limit != null) {
			int limitC = (offset != null) ? offset + limit : limit;
			readAllQuery.setMaxRows(limitC);
		}

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
	 * Convenience method
	 * 
	 * @see #executeAsStream(SingularAttribute, ORDER, Integer, Integer)
	 */
	public ScrollableCursor executeAsStream(@SuppressWarnings("rawtypes") SingularAttribute orderBy, ORDER order) {
		return executeAsStream(orderBy, order, null, null);
	}

	/**
	 * Handle the results using a stream. Please be sure to clear the cursor on
	 * each iteration in order to avoid a memory leak.
	 * 
	 * @return
	 */
	public ScrollableCursor executeAsStream() {
		return executeAsStream(null, null, null, null);
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
