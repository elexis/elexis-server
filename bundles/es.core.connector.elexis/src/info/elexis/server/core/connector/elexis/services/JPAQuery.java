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

import org.eclipse.persistence.jpa.JpaQuery;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObject;

/**
 * This class tries to resemble the Query class known by Elexis user by
 * employing JPA CriteriaQueries.
 * 
 * @param <T>
 */
public class JPAQuery<T extends AbstractDBObject> {

	public static enum QUERY {
		LIKE, EQUALS, LESS_OR_EQUAL
	};

	private Class<T> clazz;

	private CriteriaBuilder qb = em().getCriteriaBuilder();
	private CriteriaQuery<T> cq;
	private Root<T> root;
	private TypedQuery<T> query;
	
	private List<Predicate> conditions = new LinkedList<Predicate>();

	public JPAQuery(Class<T> clazz) {
		this.clazz = clazz;

		cq = qb.createQuery(clazz);
		root = cq.from(clazz);
		query = null;
	}

	public void add(String column, QUERY qt, String string) {
		Predicate pred;

		switch (qt) {
		case LIKE:
			pred = qb.like(root.get(column), string);
			break;
		case EQUALS:
			pred = qb.equal(root.get(column), string);
			break;
		case LESS_OR_EQUAL:
			Path<Integer> path = root.get(column);
			pred = qb.le(path, Integer.parseInt(string));
			break;
		default:
			throw new IllegalArgumentException();
		}
		
		conditions.add(pred);
	}

	public List<T> execute() {
		if(conditions.size()>0) {
			cq = cq.where(conditions.toArray(new Predicate[0]));
		}
		
		query = em().createQuery(cq);
		return query.getResultList();
	}

	@Override
	public String toString() {
		if(query!=null) {
			// will only print SQL string after execute()
			return query.unwrap(JpaQuery.class).getDatabaseQuery().getSQLString();
		}
		return super.toString();
	}
}
