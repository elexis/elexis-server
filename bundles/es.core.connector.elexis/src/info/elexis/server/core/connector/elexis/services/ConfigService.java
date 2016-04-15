package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Config;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Config_;

public class ConfigService {

	public static final String LIST_SEPARATOR = ",";

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

	public boolean get(String key, boolean b) {
		String string = get(key, Boolean.toString(b));
		return Boolean.valueOf(string);
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
		if (val != null && val.getWert().equalsIgnoreCase(value)) {
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

	public void remove(String key) {
		EntityManager em = ElexisEntityManager.createEntityManager();
		Config val = em.find(Config.class, key);
		if(val==null) {
			return;
		}
		
		em.getTransaction().begin();
		em.remove(val);
		em.getTransaction().commit();
		em.close();
	}
	
	/**
	 * Retrieve a value as a set.
	 * 
	 * @param key
	 * @return
	 */
	public Set<String> getAsSet(String key) {
		flush();
		Config val = findById(key);
		if (val == null) {
			return Collections.emptySet();
		}
		em.refresh(val);
		String[] split = val.getWert().split(LIST_SEPARATOR);
		return Arrays.asList(split).stream().collect(Collectors.toSet());
	}

	/**
	 * Store a set of values to a configuration key
	 * 
	 * @param key
	 * @param values
	 * @return
	 */
	public boolean setAsSet(String key, Set<String> values) {
		String flattenedValue = values.stream().map(o -> o.toString()).reduce((u, t) -> u + LIST_SEPARATOR + t).get();

		EntityManager em = ElexisEntityManager.createEntityManager();
		Config val = em.find(Config.class, key);
		if (val != null && val.getWert().equalsIgnoreCase(flattenedValue)) {
			return true;
		}

		em.getTransaction().begin();
		if (val == null) {
			val = new Config();
			val.setParam(key);
			em.persist(val);
		}
		val.setWert(flattenedValue);
		em.getTransaction().commit();
		em.close();
		return true;
	}

	/**
	 * Assert that a specific value is part of the set defined in key
	 * 
	 * @param key
	 * @param value
	 */
	public void assertPropertyInSet(String key, String value) {
		Set<String> propertySet = getAsSet(key);
		Set<String> valueSet = new HashSet<String>(propertySet);
		valueSet.add(value);
		setAsSet(key, valueSet);
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

	/**
	 * Returns a stored value as a date
	 * @param key 
	 * @return the {@link LocalDate} or <code>null</code>
	 */
	public LocalDate getAsDate(String key) {
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
