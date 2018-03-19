package info.elexis.server.findings.fhir.jpa.model.service.internal;

import static org.eclipse.persistence.config.PersistenceUnitProperties.DDL_GENERATION;
import static org.eclipse.persistence.config.PersistenceUnitProperties.NONE;

import java.util.HashMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class FindingsEntityManager {

	private static Logger logger = LoggerFactory.getLogger(FindingsEntityManager.class);

	private static EntityManagerFactoryBuilder factoryBuilder;
	private static EntityManagerFactory factory;

	@Reference(service = DataSource.class, cardinality = ReferenceCardinality.MANDATORY)
	protected synchronized void bindDataSource(DataSource dataSource) {
		// indicate data-source dependency to osgi
	}

	@Reference(service = EntityManagerFactoryBuilder.class, cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, target = "(osgi.unit.name=findings)")
	protected synchronized void bindEntityManagerFactoryBuilder(EntityManagerFactoryBuilder factoryBuilder) {
		logger.debug("Binding " + factoryBuilder.getClass().getName());
		FindingsEntityManager.factoryBuilder = factoryBuilder;
	}

	@Activate
	protected synchronized void activate() {
		HashMap<String, Object> props = new HashMap<String, Object>();
		props.put(DDL_GENERATION, NONE);
		factory = FindingsEntityManager.factoryBuilder.createEntityManagerFactory(props);
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
