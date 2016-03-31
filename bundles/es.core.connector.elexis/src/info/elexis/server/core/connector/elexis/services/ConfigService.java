package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Config;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Config_;

public class ConfigService {

	private static Logger log = LoggerFactory.getLogger(ConfigService.class);

	public static ConfigService INSTANCE = InstanceHolder.INSTANCE;

	protected EntityManager em;

	private static final class InstanceHolder {
		static final ConfigService INSTANCE = new ConfigService();
	}

	private ConfigService() {
		new Config(); // TODO refactor make sure the jpa bundle is loaded before
						// continuing
		em = ElexisEntityManager.createEntityManager();
	}

	/**
	 * Find an object by its primary id.
	 * 
	 * @param id
	 * @param entityClass
	 * @return
	 */
	public Config findById(Object id) {
		return em.find(Config.class, id);
	}

	public String get(String key, String defValue) {
		flush();
		Config val = findById(key);
		if (val != null) {
			em.refresh(val);
			return val.getWert();
		} else {
			return defValue;
		}
	}

	/**
	 * synchronous set
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean set(String key, String value) {
		EntityManager em = ElexisEntityManager.createEntityManager();
		Config val = em.find(Config.class, key);
		if(val!=null && val.getWert().equalsIgnoreCase(value)) {
			return true;
		}
		
		em.getTransaction().begin();
		if (val == null) {
			val = new Config();
			val.setParam(key);
			em.persist(val);
		}
		val.setWert(value);
		em.getTransaction().commit();
		em.close();
		return true;
	}

	/**
	 * 
	 * @param param
	 * @param performCommit
	 * @return
	 */
	public Config create(String param, final boolean performCommit) {
		Config obj = new Config();
		if (performCommit) {
			em.getTransaction().begin();
		}
		if (param != null) {
			obj.setParam(param);
		}
		em.persist(obj);
		if (performCommit) {
			em.getTransaction().commit();
		}

		return obj;
	}

	/**
	 * Create a transaction and flush all open changes onto the database
	 */
	public void flush() {
		em.getTransaction().begin();
		em.flush();
		em.getTransaction().commit();
	}

	public LocalDate getDate(String key) {
		Config value = findById(key);
		if (value != null) {
			TimeTool tt = new TimeTool(value.getWert());
			return tt.toZonedDateTime().toLocalDate();
		}
		return null;
	}

	/**
	 * Get all nodes starting with nodePrefix
	 * 
	 * @param nodePrefix
	 * @return
	 */
	public List<Config> getNodes(String nodePrefix) {
		JPAQuery<Config> query = new JPAQuery<Config>(Config.class);
		query.add(Config_.param, JPAQuery.QUERY.LIKE, nodePrefix + "%");
		return query.execute();
	}
}
