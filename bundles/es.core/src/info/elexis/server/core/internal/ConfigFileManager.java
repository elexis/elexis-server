package info.elexis.server.core.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Function;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.connector.elexis.ElexisDBConnection;

public class ConfigFileManager {

	private static ConfigFile cf;

	private static final String HOMEDIR = "elexis-server";
	private static final String FILENAME = "elexis-server-config.xml";

	private static Logger log = LoggerFactory.getLogger(ElexisDBConnection.class);

	static {
		loadConfigFile();
	}

	public static void setValue(String key, String value) {
		cf.getValues().put(key, value);
		storeConfigFile();
	}

	private static void storeConfigFile() {
		String property = System.getProperty("user.home");
		if (property == null) {
			log.error("Error in getting user.home property: null");
		}
		File dir = new File(property, HOMEDIR);
		File sdbcf = new File(dir, FILENAME);
		try (FileOutputStream fileOutputStream = new FileOutputStream(sdbcf)) {
			cf.marshall(fileOutputStream);
		} catch (IOException | JAXBException e) {
			log.error("Error storing dbconnection to " + sdbcf.getAbsolutePath(), e);
		}
	}

	private static void loadConfigFile() {
		String property = System.getProperty("user.home");
		if (property == null) {
			log.error("Error in getting user.home property: null");
		}
		File dir = new File(property, HOMEDIR);
		File sdbcf = new File(dir, FILENAME);
		if (sdbcf.exists()) {
			try (FileInputStream fis = new FileInputStream(sdbcf)) {
				cf = ConfigFile.unmarshall(fis);
				return;
			} catch (IOException | JAXBException e) {
				log.error("Error reading dbconnection from " + sdbcf.getAbsolutePath(), e);
			}
		}

		cf = new ConfigFile();
	}

	public static String getValue(String key) {
		return cf.getValues().get(key);
	}

	public static Object getValue(String key, Function<String, Object> unmarshaller) {
		String value = getValue(key);
		if (value != null) {
			return unmarshaller.apply(value);
		}
		return null;
	}
}
