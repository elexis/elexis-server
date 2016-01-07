package info.elexis.server.core.connector.elexis.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.util.DBConnection;
import info.elexis.server.core.connector.elexis.ElexisConnection;

public class ConfigFileManager {

	private static final String FILENAME = "elexis-connection.xml";

	private static Logger log = LoggerFactory.getLogger(ElexisConnection.class);

	public static void storeDBConnection(DBConnection connection) {
		String property = System.getProperty("user.home");
		if (property == null) {
			log.error("Error in getting user.home property: null");
		}
		File sdbcf = new File(property, FILENAME);
		try (FileOutputStream fileOutputStream = new FileOutputStream(sdbcf)) {
			connection.marshall(fileOutputStream);
		} catch (IOException | JAXBException e) {
			log.error("Error storing dbconnection to " + sdbcf.getAbsolutePath(), e);
		}
	}

	public static DBConnection loadStoredDBConnection() {
		String property = System.getProperty("user.home");
		if (property == null) {
			log.error("Error in getting user.home property: null");
		}
		File dir = new File(property, "elexis-server");
		File sdbcf = new File(dir, FILENAME);
		if (!sdbcf.exists())
			return null;

		try (FileInputStream fis = new FileInputStream(sdbcf)) {
			return DBConnection.unmarshall(fis);
		} catch (IOException | JAXBException e) {
			log.error("Error reading dbconnection from " + sdbcf.getAbsolutePath(), e);
			return null;
		}
	}
}
