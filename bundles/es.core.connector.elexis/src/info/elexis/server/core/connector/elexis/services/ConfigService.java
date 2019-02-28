package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Config;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Config_;

public class ConfigService {

	public static final String LIST_SEPARATOR = ",";

	private Logger log = LoggerFactory.getLogger(ConfigService.class);

	public static ConfigService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final ConfigService INSTANCE = new ConfigService();
	}

	private ConfigService() {
		new Config(); // TODO refactor make sure jpa bundle is loaded before
	}

	/**
	 * Get a stored value for a given key, or return the value provided as default
	 * 
	 * @param key
	 * @param defValue
	 *            default value if not set
	 * @return
	 */
	public String get(String key, String defValue) {
		EntityManager em = ElexisEntityManager.createEntityManager();
		try {
			Config val = em.find(Config.class, key);
			if (val != null) {
				return val.getWert();
			} else {
				return defValue;
			}
		} finally {
			em.close();
		}
	}

	/**
	 * Get a stored value for a given key as boolean, or return the value provided
	 * as default
	 * 
	 * @param key
	 * @param b
	 * @return
	 */
	public boolean get(String key, boolean defValue) {
		String string = get(key, Boolean.toString(defValue));
		if ("1".equals(string.trim())) {
			return true;
		}
		return Boolean.valueOf(string);
	}

	/**
	 * Retrieve a value as a set.
	 * 
	 * @param key
	 * @return
	 */
	public Set<String> getAsSet(String key) {
		String val = get(key, null);
		if (val == null) {
			return Collections.emptySet();
		}
		String[] split = val.split(LIST_SEPARATOR);
		return Arrays.asList(split).stream().collect(Collectors.toSet());
	}

	/**
	 * Returns a stored value as {@link LocalDate}
	 * 
	 * @param key
	 * @return the {@link LocalDate} or <code>null</code>
	 */
	public LocalDate getAsDate(String key) {
		String value = get(key, null);
		if (value != null) {
			TimeTool tt = new TimeTool(value);
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
		if (nodePrefix != null) {
			query.add(Config_.param, JPAQuery.QUERY.LIKE, nodePrefix + "%");
		}
		return query.execute();
	}

	/**
	 * Set a value for a given key
	 * 
	 * @param key
	 * @param value
	 * @return <code>true</code> if the value was successfully set
	 */
	public boolean set(String key, String value) {
		EntityManager em = ElexisEntityManager.createEntityManager();
		try {
			Config val = em.find(Config.class, key);
			if (val != null && val.getWert() != null && val.getWert().equalsIgnoreCase(value)) {
				return true;
			}
			em.getTransaction().begin();
			if (val == null) {
				val = new Config();
				val.setParam(key);
				em.persist(val);
			}
			em.lock(val, LockModeType.PESSIMISTIC_WRITE);
			val.setWert(value);
			em.getTransaction().commit();
		} catch (Exception e) {
			log.error("Error on setting config ", e);
			return false;
		} finally {
			em.close();
		}

		return true;
	}

	public boolean setFromBoolean(String key, boolean value) {
		return set(key, Boolean.toString(value));
	}

	/**
	 * Store a set of values to a configuration key
	 * 
	 * @param key
	 * @param values
	 * @return <code>true</code> if the values were successfully set
	 */
	public boolean setFromSet(String key, Set<String> values) {
		String flattenedValue = values.stream().map(o -> o.toString()).reduce((u, t) -> u + LIST_SEPARATOR + t).get();
		return set(key, flattenedValue);
	}

	public void remove(String key) {
		EntityManager em = ElexisEntityManager.createEntityManager();
		try {
			Config val = em.find(Config.class, key);
			if (val == null) {
				return;
			}

			em.getTransaction().begin();
			em.remove(val);
			em.getTransaction().commit();
		} finally {
			em.close();
		}
	}

	/**
	 * Assert that a specific value is part of the set stored in key
	 * 
	 * @param key
	 * @param value
	 */
	public void assertPropertyInSet(String key, String value) {
		Set<String> propertySet = getAsSet(key);
		Set<String> valueSet = new HashSet<String>(propertySet);
		valueSet.add(value);
		setFromSet(key, valueSet);
	}

	/**
	 * Return all elements
	 * 
	 * @return
	 */
	public static List<Config> findAllEntries() {
		EntityManager em = ElexisEntityManager.createEntityManager();
		try {
			CriteriaBuilder qb = em.getCriteriaBuilder();
			CriteriaQuery<Config> c = qb.createQuery(Config.class);
			TypedQuery<Config> q = em.createQuery(c);
			q.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);
			q.setHint(QueryHints.REFRESH, HintValues.TRUE);
			return q.getResultList();
		} finally {
			em.close();
		}
	}

}
