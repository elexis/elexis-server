package info.elexis.server.core.connector.elexis.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class ProvidedEntityManager {
	
	private static EntityManagerFactory factory;

	public static void setEntityManagerFactory(EntityManagerFactory factory) {
		ProvidedEntityManager.factory = factory;
	}

	public static EntityManager em() {
		return factory.createEntityManager();
	}
}
