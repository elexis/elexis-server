package info.elexis.server.findings.fhir.jpa.model.service.internal;

import static org.eclipse.persistence.config.PersistenceUnitProperties.CONNECTION_POOL_INTERNALLY_POOL_DATASOURCE;
import static org.eclipse.persistence.config.PersistenceUnitProperties.DDL_GENERATION;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_DRIVER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_PASSWORD;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_URL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_USER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.NONE;

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

import ch.elexis.core.common.DBConnection;
import info.elexis.server.core.connector.elexis.common.ElexisDBConnection;

@Component
public class FindingsEntityManager {

	private static Logger logger = LoggerFactory.getLogger(FindingsEntityManager.class);

	private static EntityManagerFactory factory;

	@Reference(service = EntityManagerFactoryBuilder.class, cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, target = "(osgi.unit.name=findings)")
	protected synchronized void bindEntityManagerFactoryBuilder(EntityManagerFactoryBuilder factoryBuilder) {
		Optional<DBConnection> connection = ElexisDBConnection.getConnection();
		if (!connection.isPresent()) {
			logger.error("No database-connection available");
			return;
		}

		Map<String, Object> props = new HashMap<String, Object>();
		try {
			props.put(JDBC_DRIVER, connection.get().rdbmsType.driverName);
			props.put(JDBC_URL, connection.get().connectionString);
			props.put(JDBC_USER, connection.get().username);
			props.put(JDBC_PASSWORD, connection.get().password);
			props.put(DDL_GENERATION, NONE);
			props.put(CONNECTION_POOL_INTERNALLY_POOL_DATASOURCE, Boolean.TRUE.toString());

			factory = factoryBuilder.createEntityManagerFactory(props);
		} catch (RuntimeException ite) {
			logger.error("Error creating EntityManagerFactory", ite);
		}
		logger.debug("EntityManagerFactoryBuilder set " + factoryBuilder);
	}

	protected synchronized void unbindEntityManagerFactoryBuilder(EntityManagerFactoryBuilder factoryBuilder) {
		factory = null;
	}

	public static EntityManager getEntityManager() {
		if (factory != null) {
			return factory.createEntityManager();
		}
		return null;
	}
}
