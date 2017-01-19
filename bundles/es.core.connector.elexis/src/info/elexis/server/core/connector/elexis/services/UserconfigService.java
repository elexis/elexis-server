package info.elexis.server.core.connector.elexis.services;

import java.util.Optional;

import javax.persistence.EntityManager;

import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Userconfig;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Userconfig_;

public class UserconfigService {
	
	/**
	 * Find an object by its primary id.
	 * 
	 * @param id
	 * @param entityClass
	 * @return
	 */
	public Optional<Userconfig> findById(Object id) {
		EntityManager em = ElexisEntityManager.createEntityManager();
		try {
			return Optional.ofNullable(em.find(Userconfig.class, id));
		} finally {
			em.close();
		}
	}

	public static boolean get(Kontakt userContact, String key, boolean defValue) {
		JPAQuery<Userconfig> query = new JPAQuery<Userconfig>(Userconfig.class);
		query.add(Userconfig_.owner, JPAQuery.QUERY.EQUALS, userContact);
		query.add(Userconfig_.param, JPAQuery.QUERY.EQUALS, key);
		Optional<Userconfig> result = query.executeGetSingleResult();
		if (result.isPresent()) {
			String param = result.get().getParam();
			return ("1".equals(param) || "true".equalsIgnoreCase(param));
		} else {
			return defValue;
		}
	}

}
