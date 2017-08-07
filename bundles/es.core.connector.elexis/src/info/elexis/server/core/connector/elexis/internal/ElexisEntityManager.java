package info.elexis.server.core.connector.elexis.internal;

import static org.eclipse.persistence.config.PersistenceUnitProperties.CONNECTION_POOL_INTERNALLY_POOL_DATASOURCE;
import static org.eclipse.persistence.config.PersistenceUnitProperties.DDL_GENERATION;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_DRIVER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_PASSWORD;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_URL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_USER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.NONE;

import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.constants.Preferences;
import info.elexis.server.core.connector.elexis.common.ElexisDBConnection;
import info.elexis.server.core.connector.elexis.jpa.ProvidedEntityManager;
import info.elexis.server.core.connector.elexis.services.ConfigService;

@Component
public class ElexisEntityManager {

	private static Logger log = LoggerFactory.getLogger(ElexisEntityManager.class);

	private static EntityManagerFactoryBuilder factoryBuilder;
	private static EntityManagerFactory factory;

	@Reference(service = EntityManagerFactoryBuilder.class, cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, target = "(osgi.unit.name=elexis)")
	protected synchronized void bind(EntityManagerFactoryBuilder factoryBuilder) {
		log.debug("Binding " + factoryBuilder.getClass().getName());
		ElexisEntityManager.factoryBuilder = factoryBuilder;
	}

	@Activate
	protected synchronized void activate() {
		Optional<DBConnection> connection = ElexisDBConnection.getConnection();
		if (!connection.isPresent()) {
			log.error("No elexis-connection available, not initialization EntityManager");
			return;
		}
		
		initializeEntityManager(connection.get());
		if (!ElexisDBConnection.isTestMode()) {
			executeStartupTasksRequiringEntityManager();
		}
	}

	protected synchronized void unbind(EntityManagerFactoryBuilder factoryBuilder) {
		log.debug("Unbinding " + factoryBuilder.getClass().getName());
		ElexisEntityManager.factoryBuilder = null;
	}

	private void initializeEntityManager(DBConnection connection) {
		HashMap<String, Object> props = new HashMap<String, Object>();
		try {
			props.put(JDBC_DRIVER, connection.rdbmsType.driverName);
			props.put(JDBC_URL, connection.connectionString);
			props.put(JDBC_USER, connection.username);
			props.put(JDBC_PASSWORD, connection.password);
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
			log.error("Error configuration database connection parameters.", ite);
			ite.printStackTrace();
		}
	}

	private void executeStartupTasksRequiringEntityManager() {
		// check locale
		Locale locale = Locale.getDefault();
		String dbStoredLocale = ConfigService.INSTANCE.get(Preferences.CFG_LOCALE, null);
		if (dbStoredLocale == null || !locale.toString().equals(dbStoredLocale)) {
			System.out.println("System locale does not match required database locale!");
			log.error("System locale [{}] does not match required database locale [{}].", locale.toString(),
					dbStoredLocale);
		}
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
