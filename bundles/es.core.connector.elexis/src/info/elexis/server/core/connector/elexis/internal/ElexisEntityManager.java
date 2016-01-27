package info.elexis.server.core.connector.elexis.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.connector.elexis.jpa.ProvidedEntityManager;

@Component
public class ElexisEntityManager {

	private static Logger log = LoggerFactory.getLogger(ElexisEntityManager.class);

	private static EntityManagerFactoryBuilder factoryBuilder;
	private static EntityManagerFactory factory;
	private static EntityManager em;

	@Reference(
			service = EntityManagerFactoryBuilder.class, 
			cardinality = ReferenceCardinality.MANDATORY, 
			policy = ReferencePolicy.STATIC, 
			unbind = "deactivate",
			target = "(osgi.unit.name=elexis)")
	protected synchronized void activate(EntityManagerFactoryBuilder factoryBuilder) {
		ElexisEntityManager.factoryBuilder = factoryBuilder;
		ElexisEntityManager.initializeEntityManager();
	}

	protected static void initializeEntityManager() {
		Optional<DBConnection> connection = ElexisDBConnection.getConnection();
		if(!connection.isPresent()) {
			log.error("No elexis-connection available, not initialization EntityManager");
			return;
		}	
		
		Map<String, Object> props = new HashMap<String, Object>();
		try {			
				props.put("javax.persistence.jdbc.driver", connection.get().rdbmsType.driverName);
				props.put("javax.persistence.jdbc.url", connection.get().connectionString);
				props.put("javax.persistence.jdbc.user", connection.get().username);
				props.put("javax.persistence.jdbc.password", connection.get().password);
				
			factory = ElexisEntityManager.factoryBuilder.createEntityManagerFactory(props);
			em = factory.createEntityManager();
			
			// TODO refactor this
			ProvidedEntityManager.setEntityManager(em);
		} catch (RuntimeException ite) {
			log.error("Error activating component", ite);
			ite.printStackTrace();
		}
	}

	protected synchronized void deactivate(EntityManagerFactoryBuilder factoryBuilder) {
		ElexisEntityManager.factoryBuilder = null;
		if (em() != null) {
			em.close();
		}
	}

	public static EntityManager em() {
		return em;
	}
}
