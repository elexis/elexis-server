package info.elexis.server.core.connector.elexis.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.services.IElexisDataSource;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.status.StatusUtil;
import info.elexis.server.core.common.util.CoreUtil;
import info.elexis.server.core.connector.elexis.internal.Activator;

public class ElexisDBConnectionUtil {

	private static DBConnection connection;
	private static Path connectionConfigPath;

	private static Logger log = LoggerFactory.getLogger(ElexisDBConnectionUtil.class);

	static {
		connectionConfigPath = CoreUtil.getHomeDirectory().resolve("elexis-connection.xml");
		if (connectionConfigPath.toFile().exists()) {
			try (InputStream is = Files.newInputStream(connectionConfigPath, StandardOpenOption.READ)) {
				connection = DBConnection.unmarshall(is);
				log.info("Initialized elexis connection from " + connectionConfigPath.toAbsolutePath());
				StatusUtil.logStatus(log, verifyConnection(connection));
			} catch (IOException | JAXBException e) {
				log.warn("Error opening " + connectionConfigPath.toAbsolutePath(), e);
			}
		}
	}

	public static Optional<DBConnection> getConnection() {
		return Optional.ofNullable(connection);
	}

	public static IStatus setConnection(IElexisDataSource elexisDataSource, DBConnection connection) {
		IStatus status = verifyConnection(connection);
		if (status.isOK()) {
			try {
				status = doSetConnection(elexisDataSource, connection);
			} catch (IOException | JAXBException | InterruptedException e) {
				return new Status(Status.ERROR, Activator.BUNDLE_ID, "Error setting database connection", e);
			}
		}
		return status;
	}

	/**
	 * Verify if the provided connection matches the Elexis-Server requirements.
	 * 
	 * @param dbConnection
	 * @return
	 */
	private static IStatus verifyConnection(DBConnection dbConnection) {
		if (dbConnection == null) {
			return new Status(Status.ERROR, Activator.BUNDLE_ID, "Connection is null");
		}

		try {
			switch (dbConnection.rdbmsType) {
			case H2:
				Class.forName("org.h2.Driver").newInstance();
				break;
			case MySQL:
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				break;
			case PostgreSQL:
				Class.forName("org.postgresql.Driver").newInstance();
				break;
			default:
				break;
			}
		} catch (Exception e) {
			return new Status(Status.ERROR, Activator.BUNDLE_ID, e.getMessage());
		}

		try (Connection connection = DriverManager.getConnection(dbConnection.connectionString, dbConnection.username,
				dbConnection.password)) {
			boolean valid = connection.isValid(10);
			if (!valid) {
				return new Status(Status.ERROR, Activator.BUNDLE_ID, "Invalid connection");
			}
		} catch (SQLException e) {
			return new Status(Status.ERROR, Activator.BUNDLE_ID, e.getMessage());
		}

		return Status.OK_STATUS;
	}

	private static IStatus doSetConnection(IElexisDataSource elexisDataSource, DBConnection connection)
			throws IOException, JAXBException, InterruptedException {
		ElexisDBConnectionUtil.connection = connection;
		persistDBConnection();
		IStatus elexisDataSourceStatus = elexisDataSource.setDBConnection(connection);
		if (elexisDataSourceStatus.isOK()) {
			ServiceTracker<Object, Object> serviceTracker = new ServiceTracker<>(Activator.getContext(),
					IModelService.class.getName(), null);
			serviceTracker.waitForService(5000);
		}
		return elexisDataSourceStatus;
	}

	private static void persistDBConnection() throws IOException, JAXBException {
		if (connection != null) {
			try (OutputStream fos = Files.newOutputStream(connectionConfigPath, StandardOpenOption.CREATE)) {
				connection.marshall(fos);
			}
		}
	}

}
