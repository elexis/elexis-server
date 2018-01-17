package info.elexis.server.core.connector.elexis.datasource.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.common.DBConnection.DBType;
import ch.elexis.core.status.StatusUtil;
import info.elexis.server.core.common.util.CoreUtil;
import info.elexis.server.core.connector.elexis.datasource.internal.Activator;

public class ElexisDBConnectionUtil {

	private static DBConnection connection;
	private static Path connectionConfigPath;

	private static Logger log = LoggerFactory.getLogger(ElexisDBConnectionUtil.class);

	static {
		connectionConfigPath = CoreUtil.getHomeDirectory().resolve("elexis-connection.xml");
		if (isTestMode()) {
			connection = ElexisDBConnectionUtil.getTestDatabaseConnection();
			setConnection(connection);
		} else if (connectionConfigPath.toFile().exists()) {
			try (InputStream is = Files.newInputStream(connectionConfigPath, StandardOpenOption.READ)) {
				connection = DBConnection.unmarshall(is);
				log.info("Initialized elexis connection from " + connectionConfigPath.toAbsolutePath());
				StatusUtil.logStatus(log, setConnection(connection));
			} catch (IOException | JAXBException e) {
				log.warn("Error opening " + connectionConfigPath.toAbsolutePath(), e);
			}
		}
	}

	public static Optional<DBConnection> getConnection() {
		return Optional.ofNullable(connection);
	}

	public static IStatus setConnection(DBConnection connection) {
		IStatus status = verifyConnection(connection);
		if (status.isOK()) {
			try {
				doSetConnection(connection);
			} catch (IOException | JAXBException e) {
				return new Status(Status.ERROR, Activator.BUNDLE_ID, "error persisting database connection", e);
			}
		}
		return status;
	}

	/**
	 * Verify if the provided connection matches the Elexis-Server requirements
	 * 
	 * @param connection
	 * @return
	 */
	private static IStatus verifyConnection(DBConnection connection) {
		// TODO Auto-generated method stub
		return Status.OK_STATUS;
	}

	private static void doSetConnection(DBConnection connection) throws IOException, JAXBException {
		ElexisDBConnectionUtil.connection = connection;
		Activator.refreshDataSource();
		persistDBConnection();
	}

	private static void persistDBConnection() throws IOException, JAXBException {
		if (connection != null) {
			try (OutputStream fos = Files.newOutputStream(connectionConfigPath, StandardOpenOption.CREATE)) {
				connection.marshall(fos);
			}
		}
	}

	public static boolean isTestMode() {
		String testMode = System.getProperty("es.test");
		if (testMode != null && !testMode.isEmpty()) {
			if (testMode.equalsIgnoreCase("true")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return an h2 based test database connection
	 */
	public static DBConnection getTestDatabaseConnection() {
		DBConnection retVal = new DBConnection();
		retVal.connectionString = "jdbc:h2:mem:elexisTest;DB_CLOSE_DELAY=-1";
		retVal.rdbmsType = DBType.H2;
		retVal.username = "sa";
		retVal.password = "";
		return retVal;
	}
}
