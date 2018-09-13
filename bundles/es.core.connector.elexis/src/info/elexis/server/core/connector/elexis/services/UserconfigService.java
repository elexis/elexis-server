package info.elexis.server.core.connector.elexis.services;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Userconfig;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Userconfig_;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;

public class UserconfigService {

	public static class Builder {
		private Userconfig object;

		public Builder(Kontakt owner, String param, String value) {
			object = new Userconfig();
			object.setOwnerId(owner.getId());
			object.setParam(param);
			object.setValue(value);
		}

		public Userconfig buildAndSave() {
			return save();
		}

		private Userconfig save() {
			EntityManager em = ElexisEntityManager.createEntityManager();
			try {
				em.getTransaction().begin();
				object = em.merge(object);
				em.getTransaction().commit();
				return object;
			} finally {
				em.close();
			}
		}
	}

	public static boolean get(Kontakt userContact, String param, boolean defValue) {
		JPAQuery<Userconfig> query = new JPAQuery<Userconfig>(Userconfig.class);
		if (userContact != null) {
			query.add(Userconfig_.ownerId, JPAQuery.QUERY.EQUALS, userContact.getId());
		}
		query.add(Userconfig_.param, JPAQuery.QUERY.EQUALS, param);
		Optional<Userconfig> result = query.executeGetSingleResult();
		if (result.isPresent()) {
			String value = result.get().getValue();
			return ("1".equals(value) || "true".equalsIgnoreCase(value));
		} else {
			return defValue;
		}
	}

	public static String get(Kontakt userContact, String param, String defValue) {
		JPAQuery<Userconfig> query = new JPAQuery<Userconfig>(Userconfig.class);
		query.add(Userconfig_.ownerId, JPAQuery.QUERY.EQUALS, userContact.getId());
		query.add(Userconfig_.param, JPAQuery.QUERY.EQUALS, param);
		Optional<Userconfig> result = query.executeGetSingleResult();
		if (result.isPresent()) {
			return result.get().getValue();
		} else {
			return defValue;
		}
	}

	public static List<Userconfig> findAllEntries() {
		EntityManager em = ElexisEntityManager.createEntityManager();
		try {
			CriteriaBuilder qb = em.getCriteriaBuilder();
			CriteriaQuery<Userconfig> c = qb.createQuery(Userconfig.class);
			TypedQuery<Userconfig> q = em.createQuery(c);
			q.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);
			q.setHint(QueryHints.REFRESH, HintValues.TRUE);
			return q.getResultList();
		} finally {
			em.close();
		}
	}

	/**
	 * Get all nodes starting with nodePrefix
	 * 
	 * @param nodePrefix
	 * @param contact
	 *            the contact to get the nodes for. Can be <code>null</code>
	 * @return
	 */
	public static List<Userconfig> getNodes(String nodePrefix, Kontakt contact) {
		JPAQuery<Userconfig> query = new JPAQuery<Userconfig>(Userconfig.class);
		if (nodePrefix != null) {
			query.add(Userconfig_.param, JPAQuery.QUERY.LIKE, nodePrefix + "%");
		}
		if (contact != null) {
			query.add(Userconfig_.ownerId, QUERY.EQUALS, contact.getId());
		}
		return query.execute();
	}

	public static List<Userconfig> findAllEntries(Kontakt contact) {
		return getNodes("", contact);
	}
}
