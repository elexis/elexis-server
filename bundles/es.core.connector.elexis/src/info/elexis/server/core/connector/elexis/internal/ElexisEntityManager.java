package info.elexis.server.core.connector.elexis.internal;

import static org.eclipse.persistence.config.PersistenceUnitProperties.*;

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

	@Reference(service = EntityManagerFactoryBuilder.class, cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "deactivate", target = "(osgi.unit.name=elexis)")
	protected synchronized void activate(EntityManagerFactoryBuilder factoryBuilder) {
		ElexisEntityManager.factoryBuilder = factoryBuilder;
		ElexisEntityManager.initializeEntityManager();
	}

	public static void initializeEntityManager() {
		Optional<DBConnection> connection = ElexisDBConnection.getConnection();
		if (!connection.isPresent()) {
			log.error("No elexis-connection available, not initialization EntityManager");
			return;
		}

		HashMap<String, Object> props = new HashMap<String, Object>();
		try {
			props.put(JDBC_DRIVER, connection.get().rdbmsType.driverName);
			props.put(JDBC_URL, connection.get().connectionString);
			props.put(JDBC_USER, connection.get().username);
			props.put(JDBC_PASSWORD, connection.get().password);
			props.put(DDL_GENERATION, NONE);
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=379397
			props.put(CONNECTION_POOL_INTERNALLY_POOL_DATASOURCE, Boolean.TRUE.toString());
			if (ElexisDBConnection.isTestMode()) {
				// we don't want the entities to generate the database, as
				// initialization is handled via the creation scripts
				props.put("eclipselink.ddl-generation", "none");
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
		if (factory != null) {
			return factory.getCriteriaBuilder();
		} else {
			System.out.println("Trying to create CriteriaBuilder on null EntityManagerFactory");
			log.error("Trying to create CriteriaBuilder on null EntityManagerFactory");
		}
		return null;

	}

	public static EntityManager createEntityManager() {
		if (factory != null) {
			return factory.createEntityManager();
		} else {
			System.out.println("Trying to create EntityManager on null EntityManagerFactory");
			log.error("Trying to create EntityManager on null EntityManagerFactory");
		}
		return null;
	}
}
