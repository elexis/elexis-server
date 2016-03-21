package info.elexis.server.core.connector.elexis.internal;

import java.util.HashMap;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.connector.elexis.common.DBConnection;
import info.elexis.server.core.connector.elexis.common.ElexisDBConnection;
import info.elexis.server.core.connector.elexis.jpa.ProvidedEntityManager;

@Component
public class ElexisEntityManager {

	private static Logger log = LoggerFactory.getLogger(ElexisEntityManager.class);

	private static EntityManagerFactoryBuilder factoryBuilder;
	private static EntityManagerFactory factory;

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

	public static void initializeEntityManager() {
		Optional<DBConnection> connection = ElexisDBConnection.getConnection();
		if(!connection.isPresent()) {
			log.error("No elexis-connection available, not initialization EntityManager");
			return;
		}	
		
		HashMap<String, Object> props = new HashMap<String, Object>();
		try {
			props.put("javax.persistence.jdbc.driver", connection.get().rdbmsType.driverName);
			props.put("javax.persistence.jdbc.url", connection.get().connectionString);
			props.put("javax.persistence.jdbc.user", connection.get().username);
			props.put("javax.persistence.jdbc.password", connection.get().password);
			props.put("eclipselink.ddl-generation", "none");
			if (ElexisDBConnection.isTestMode()) {
				props.put("eclipselink.ddl-generation", "drop-and-create-tables");
				props.put("eclipselink.ddl-generation.output-mode", "database");
			}

			factory = ElexisEntityManager.factoryBuilder.createEntityManagerFactory(props);
			// TODO refactor this
			ProvidedEntityManager.setEntityManagerFactory(factory);
		} catch (RuntimeException ite) {
			log.error("Error activating component", ite);
			ite.printStackTrace();
		}
	}

	protected synchronized void deactivate(EntityManagerFactoryBuilder factoryBuilder) {
		ElexisEntityManager.factoryBuilder = null;
	}
	
	public static CriteriaBuilder getCriteriaBuilder() {
		return factory.getCriteriaBuilder();
	}

	public static EntityManager createEntityManager() {
		return factory.createEntityManager();
	}
}
