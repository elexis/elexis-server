package info.elexis.server.core.connector.elexis.sys;

import java.util.Collection;
import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObject;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Role;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.User;

public class Elexis {
	public static boolean userHasRole(User u, String role) {
		if (u == null || role == null) {
			throw new IllegalArgumentException();
		}
		Collection<Role> roles = u.getRoles();
		long count = roles.stream().filter(f -> role.equalsIgnoreCase(f.getId())).count();
		return (count > 0l);
	}

	public static List<User> getAllUsers() {
		return getAll(User.class);
	}

	public static <T extends AbstractDBObject> List<T> getAll(Class<T> type) {
		CriteriaBuilder qb = ElexisEntityManager.em().getCriteriaBuilder();
		CriteriaQuery<T> c = qb.createQuery(type);
		TypedQuery<T> q = ElexisEntityManager.em().createQuery(c);
		return q.getResultList();
	}
}
