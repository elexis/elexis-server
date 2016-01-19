package info.elexis.server.core.connector.elexis.jpa;

import javax.persistence.EntityManager;

public class ProvidedEntityManager {
	
	private static EntityManager em;

	public static void setEntityManager(EntityManager em) {
		ProvidedEntityManager.em = em;
	}

	public static EntityManager em() {
		return em;
	}
}
