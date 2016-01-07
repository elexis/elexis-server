package info.elexis.server.core.connector.elexis.jpa.manager;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;

import org.osgi.service.jpa.EntityManagerFactoryBuilder;

public class ElexisEntityManager {

	private static EntityManagerFactoryBuilder factoryBuilder;
	private static EntityManagerFactory factory;
	private static EntityManager em;

	public ElexisEntityManager() {
	}

	public synchronized void activate(EntityManagerFactoryBuilder factoryBuilder) {
		ElexisEntityManager.factoryBuilder = factoryBuilder;

		Map<String, Object> props = new HashMap<String, Object>();
		try {
			factory = factoryBuilder.createEntityManagerFactory(props);
			em = factory.createEntityManager();
		} catch (RuntimeException ite) {
			ite.printStackTrace();
		}
	}

	public synchronized void deactivate(EntityManagerFactoryBuilder factoryBuilder) {
		ElexisEntityManager.factoryBuilder = null;
		em.close();
	}

	public synchronized void bind(PersistenceUnitUtil puUtil) {
		System.out.println(puUtil);
	}

	public static EntityManager getEntityManager() {
		return em;
	}

	public static EntityManagerFactory getEntityManagerFactory() {
		return factory;
	}
}
