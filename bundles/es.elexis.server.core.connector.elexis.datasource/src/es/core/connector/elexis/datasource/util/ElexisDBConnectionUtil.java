package es.core.connector.elexis.datasource.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.common.DBConnection.DBType;
import info.elexis.server.core.common.util.CoreUtil;

public class ElexisDBConnectionUtil {
	
	private static DBConnection connection;
	private static Path connectionConfigPath;
	
	private static Logger log = LoggerFactory.getLogger(ElexisDBConnectionUtil.class);
	
	static {
		connectionConfigPath = CoreUtil.getHomeDirectory().resolve("elexis-connection.xml");
		if (isTestMode()) {
			connection = new DBConnection();
			connection.connectionString = "jdbc:h2:mem:elexisTest;DB_CLOSE_DELAY=-1";
			connection.rdbmsType = DBType.H2;
			connection.username = "sa";
			connection.password = "";
			setConnection(connection);
		} else if (connectionConfigPath.toFile().exists()) {
			try {
				InputStream is =
					Files.newInputStream(connectionConfigPath, StandardOpenOption.READ);
				connection = DBConnection.unmarshall(is);
				is.close();
				log.info(
					"Initialized elexis connection from " + connectionConfigPath.toAbsolutePath());
				setConnection(connection);
			} catch (IOException | JAXBException e) {
				log.warn("Error opening " + connectionConfigPath.toAbsolutePath(), e);
			}
		}
	}
	
	public static Optional<DBConnection> getConnection(){
		return Optional.ofNullable(connection);
	}
	
	private static void setConnection(DBConnection connection){
		ElexisDBConnectionUtil.connection = connection;
	}
	
	public static boolean isTestMode(){
		String testMode = System.getProperty("es.test");
		if (testMode != null && !testMode.isEmpty()) {
			if (testMode.equalsIgnoreCase("true")) {
				return true;
			}
		}
		return false;
	}
}
