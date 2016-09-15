package info.elexis.server.findings.fhir.jpa.service.internal;

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

import info.elexis.server.core.connector.elexis.common.DBConnection;
import info.elexis.server.core.connector.elexis.common.ElexisDBConnection;

@Component
public class FindingsEntityManager {

	private static Logger log = LoggerFactory.getLogger(FindingsEntityManager.class);

	private static EntityManagerFactory factory;

	@Reference(service = EntityManagerFactoryBuilder.class, cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, target = "(osgi.unit.name=findings)")
	protected synchronized void bindEntityManagerFactoryBuilder(EntityManagerFactoryBuilder factoryBuilder) {
		Optional<DBConnection> connection = ElexisDBConnection.getConnection();
		if (!connection.isPresent()) {
			log.error("No database-connection available");
			return;
		}

		Map<String, Object> props = new HashMap<String, Object>();
		try {
			props.put("javax.persistence.jdbc.driver", connection.get().rdbmsType.driverName);
			props.put("javax.persistence.jdbc.url", connection.get().connectionString);
			props.put("javax.persistence.jdbc.user", connection.get().username);
			props.put("javax.persistence.jdbc.password", connection.get().password);

			factory = factoryBuilder.createEntityManagerFactory(props);
		} catch (RuntimeException ite) {
			log.error("Error creating EntityManagerFactory", ite);
		}
	}

	protected synchronized void unbindEntityManagerFactoryBuilder(EntityManagerFactoryBuilder factoryBuilder) {
		factory = null;
	}

	public static EntityManager getEntityManager() {
		if (factory != null) {
			return factory.createEntityManager();
		}
		throw new IllegalStateException("No EntityManagerFactory available");
	}
}
