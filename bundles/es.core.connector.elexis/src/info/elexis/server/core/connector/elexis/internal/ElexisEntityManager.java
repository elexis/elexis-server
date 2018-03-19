package info.elexis.server.core.connector.elexis.internal;

import static org.eclipse.persistence.config.PersistenceUnitProperties.DDL_GENERATION;
import static org.eclipse.persistence.config.PersistenceUnitProperties.NONE;

import java.util.HashMap;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.sql.DataSource;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import info.elexis.server.core.connector.elexis.common.ElexisDBConnection;
import info.elexis.server.core.connector.elexis.jpa.ProvidedEntityManager;
import info.elexis.server.core.connector.elexis.services.ConfigService;

@Component
public class ElexisEntityManager {

	private static Logger log = LoggerFactory.getLogger(ElexisEntityManager.class);

	private static EntityManagerFactoryBuilder factoryBuilder;
	private static EntityManagerFactory factory;

	@Reference(service = DataSource.class, cardinality = ReferenceCardinality.MANDATORY)
	protected synchronized void bindDataSource(DataSource dataSource) {
		// indicate data-source dependency to osgi
	}
	
	@Reference(service = EntityManagerFactoryBuilder.class, cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, target = "(osgi.unit.name=elexis)")
	protected synchronized void bind(EntityManagerFactoryBuilder factoryBuilder) {
		log.debug("Binding " + factoryBuilder.getClass().getName());
		ElexisEntityManager.factoryBuilder = factoryBuilder;
	}

	@Activate
	protected synchronized void activate() {		
		HashMap<String, Object> props = new HashMap<String, Object>();
		props.put(DDL_GENERATION, NONE);
		factory = ElexisEntityManager.factoryBuilder.createEntityManagerFactory(props);
		ProvidedEntityManager.setEntityManagerFactory(factory);
		
		if (!ElexisDBConnection.isTestMode()) {
			executeStartupTasksRequiringEntityManager();
		}
	}

	protected synchronized void unbind(EntityManagerFactoryBuilder factoryBuilder) {
		log.debug("Unbinding " + factoryBuilder.getClass().getName());
		ElexisEntityManager.factoryBuilder = null;
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
