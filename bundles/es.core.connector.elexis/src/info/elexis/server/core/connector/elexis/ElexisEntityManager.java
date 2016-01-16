package info.elexis.server.core.connector.elexis;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ElexisEntityManager {

	private Logger log = LoggerFactory.getLogger(ElexisEntityManager.class);

	private static EntityManagerFactoryBuilder factoryBuilder;
	private static EntityManagerFactory factory;
	private static EntityManager em;

	public ElexisEntityManager() {
	}

	@Reference(
			service = EntityManagerFactoryBuilder.class, 
			cardinality = ReferenceCardinality.OPTIONAL, 
			policy = ReferencePolicy.STATIC, 
			unbind = "deactivate")
	public synchronized void activate(EntityManagerFactoryBuilder factoryBuilder) {
		ElexisEntityManager.factoryBuilder = factoryBuilder;

		Map<String, Object> props = new HashMap<String, Object>();
		try {
			factory = factoryBuilder.createEntityManagerFactory(props);
			em = factory.createEntityManager();
		} catch (RuntimeException ite) {
			log.error("Error activating component", ite);
			ite.printStackTrace();
		}
	}

	public synchronized void deactivate(EntityManagerFactoryBuilder factoryBuilder) {
		ElexisEntityManager.factoryBuilder = null;
		if (em() != null) {
			em.close();
		}
	}

	public static EntityManager em() {
		return em;
	}

	public static EntityManagerFactory getEntityManagerFactory() {
		return factory;
	}
}
