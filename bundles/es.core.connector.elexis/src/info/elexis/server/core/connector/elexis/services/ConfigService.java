package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ch.elexis.core.model.IConfig;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.rgw.tools.TimeTool;

public class ConfigService {
	
	public static final String LIST_SEPARATOR = ",";
	
	public static ConfigService INSTANCE = InstanceHolder.INSTANCE;
	
	private static final class InstanceHolder {
		static final ConfigService INSTANCE = new ConfigService();
	}
	
	private static IModelService modelService =
		OsgiServiceUtil.getService(IModelService.class).get();
	
	/**
	 * Get a stored value for a given key, or return the value provided as default
	 * 
	 * @param key
	 * @param defValue
	 *            default value if not set
	 * @return
	 */
	public String get(String key, String defValue){
		Optional<IConfig> configEntry = modelService.load(key, IConfig.class);
		return configEntry.map(v -> v.getValue()).orElse(defValue);
	}
	
	/**
	 * Get a stored value for a given key as boolean, or return the value provided as default
	 * 
	 * @param key
	 * @param b
	 * @return
	 */
	public boolean get(String key, boolean defValue){
		String string = get(key, Boolean.toString(defValue));
		return Boolean.valueOf(string);
	}
	
	/**
	 * Retrieve a value as a set.
	 * 
	 * @param key
	 * @return
	 */
	public Set<String> getAsSet(String key){
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
	public LocalDate getAsDate(String key){
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
	public List<IConfig> getNodes(String nodePrefix){
		IQuery<IConfig> query = modelService.getQuery(IConfig.class);
		if (nodePrefix != null) {
			query.and(ModelPackage.Literals.ICONFIG__KEY, COMPARATOR.LIKE, nodePrefix + "%");
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
	public boolean set(String key, String value){
		IConfig entry =
			modelService.load(key, IConfig.class).orElse(modelService.create(IConfig.class));
		entry.setKey(key);
		entry.setValue(value);
		return modelService.save(entry);
	}
	
	public boolean setFromBoolean(String key, boolean value){
		return set(key, Boolean.toString(value));
	}
	
	/**
	 * Store a set of values to a configuration key
	 * 
	 * @param key
	 * @param values
	 * @return <code>true</code> if the values were successfully set
	 */
	public boolean setFromSet(String key, Set<String> values){
		String flattenedValue =
			values.stream().map(o -> o.toString()).reduce((u, t) -> u + LIST_SEPARATOR + t).get();
		return set(key, flattenedValue);
	}
	
	public void remove(String key){
		Optional<IConfig> entry = modelService.load(key, IConfig.class);
		if (entry.isPresent()) {
			modelService.remove(entry.get());
		}
	}
	
	/**
	 * Assert that a specific value is part of the set stored in key
	 * 
	 * @param key
	 * @param value
	 */
	public void assertPropertyInSet(String key, String value){
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
	public static List<IConfig> findAllEntries(){
		return modelService.getQuery(IConfig.class).execute();
	}
	
}
