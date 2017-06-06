package info.elexis.server.core.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.common.util.CoreUtil;

public class LocalProperties {
	private static Logger log = LoggerFactory.getLogger(LocalProperties.class);
	private static File propertiesFile = CoreUtil.getHomeDirectory().resolve("elexis-server.properties").toFile();

	private static final String LIST_SEPARATOR = ",";

	private static Properties properties = new Properties();

	static {
		try (FileInputStream fis = new FileInputStream(propertiesFile)) {
			properties.load(fis);
		} catch (FileNotFoundException fne) {
			log.info("Properties file {} not found, will create on first save.", propertiesFile.getAbsolutePath());
		} catch (IOException e) {
			log.error("Error loading elexis-server properties file: {}.", propertiesFile.getAbsolutePath(), e);
		}
	}

	public static void store() {
		try (FileOutputStream fis = new FileOutputStream(propertiesFile)) {
			properties.store(fis, "Elexis-Server properties");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			log.error("Error writing elexis-server properties file: {}", propertiesFile.getAbsolutePath(), e);
		}
	}

	public static void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}

	public static void setProperty(String key, boolean value) {
		setProperty(key, Boolean.toString(value));
	}

	public static String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	/**
	 * 
	 * @param key
	 * @param defaultValue
	 *            if invalidValue or no key entry found
	 * @return
	 */
	public static boolean getPropertyAsBoolean(String key, boolean defaultValue) {
		String property = properties.getProperty(key);
		if (property != null) {
			return Boolean.valueOf(property);
		}
		return defaultValue;
	}

	public static Set<String> getPropertyAsSet(String key) {
		String property = getProperty(key, null);
		if (property == null) {
			return Collections.emptySet();
		}
		String[] split = property.split(LIST_SEPARATOR);
		return Arrays.asList(split).stream().collect(Collectors.toSet());
	}

	public static void setPropertyAsSet(String key, Set<String> values) {
		String flattenedValue = values.stream().map(o -> o.toString()).reduce((u, t) -> u + LIST_SEPARATOR + t).get();
		properties.setProperty(key, flattenedValue);
	}

	public static void removeProperty(String key) {
		properties.remove(key);
	}

	public static void assertPropertyInSet(String key, String value) {
		Set<String> propertySet = getPropertyAsSet(key);
		Set<String> valueSet = new HashSet<String>(propertySet);
		valueSet.add(value);
		setPropertyAsSet(key, valueSet);
		store();
	}
}
