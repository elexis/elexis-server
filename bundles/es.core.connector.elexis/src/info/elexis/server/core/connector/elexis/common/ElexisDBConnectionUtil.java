package info.elexis.server.core.connector.elexis.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
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
			try (InputStream is =
				Files.newInputStream(connectionConfigPath, StandardOpenOption.READ)) {
				connection = DBConnection.unmarshall(is);
				log.info(
					"Initialized elexis connection from " + connectionConfigPath.toAbsolutePath());
				StatusUtil.logStatus(log, verifyConnection(connection));
			} catch (IOException | JAXBException e) {
				log.warn("Error opening " + connectionConfigPath.toAbsolutePath(), e);
			}
		}
	}
	
	public static Optional<DBConnection> getConnection(){
		return Optional.ofNullable(connection);
	}
	
	public static IStatus setConnection(IElexisDataSource elexisDataSource,
		DBConnection connection){
		IStatus status = verifyConnection(connection);
		if (status.isOK()) {
			try {
				status = doSetConnection(elexisDataSource, connection);
			} catch (IOException | JAXBException | InterruptedException e) {
				return new Status(Status.ERROR, Activator.BUNDLE_ID,
					"Error setting database connection", e);
			}
		}
		return status;
	}
	
	/**
	 * Verify if the provided connection matches the Elexis-Server requirements.
	 * 
	 * @param connection
	 * @return
	 */
	private static IStatus verifyConnection(DBConnection connection){
		if (connection == null) {
			return new Status(Status.ERROR, Activator.BUNDLE_ID, "Connection is null");
		}
		
		Connection dbConnection = null;
		try {
			switch (connection.rdbmsType) {
			case H2:
				Class.forName("org.h2.Driver");
				break;
			case MySQL:
				Class.forName("com.mysql.jdbc.Driver");
				break;
			case PostgreSQL:
				Class.forName("org.postgresql.Driver");
				break;
			default:
				break;
			}
			
//			dbConnection = DriverManager.getConnection(connection.connectionString,
//				connection.username, connection.password);
//			Statement statement;
//			String validationQuery = "SELECT wert FROM config WHERE param = 'dbversion'";
//			
//			statement = dbConnection.createStatement();
//			
//			ResultSet executeQuery = statement.executeQuery(validationQuery);
//			if (executeQuery.next()) {
//				String dbVersionValue = executeQuery.getString(1);
//				if (dbVersionValue == null || dbVersionValue.length() < 4) {
//					return new Status(Status.ERROR, Activator.BUNDLE_ID,
//						"Invalid database version [" + dbVersionValue + "] found.");
//				}
//				//				Semver dbVersion = new Semver(dbVersionValue);
//				//				if (dbVersion.isGreaterThanOrEqualTo(MINIMUM_REQUIRED_DB_VERSION)) {
//				return Status.OK_STATUS;
//				//				} else {
//				//					return new Status(Status.ERROR, Activator.BUNDLE_ID, "Minimum required db version is ["
//				//							+ MINIMUM_REQUIRED_DB_VERSION + "] found db version [" + dbVersionValue + "]");
//				//				}
//			} else {
//				return new Status(Status.ERROR, Activator.BUNDLE_ID,
//					"No dbversion entry found in config table.");
//			}
			return Status.OK_STATUS;
		} catch (ClassNotFoundException  cnfe) {
			return new Status(Status.ERROR, Activator.BUNDLE_ID, cnfe.getMessage());
		} finally {
			if (dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException e) {
					log.error("Error closing dbConnection", e);
				}
			}
		}
	}
	
	private static IStatus doSetConnection(IElexisDataSource elexisDataSource,
		DBConnection connection) throws IOException, JAXBException, InterruptedException{
		ElexisDBConnectionUtil.connection = connection;
		persistDBConnection();
		IStatus elexisDataSourceStatus = elexisDataSource.setDBConnection(connection);
		if (elexisDataSourceStatus.isOK()) {
			ServiceTracker<Object, Object> serviceTracker =
				new ServiceTracker<>(Activator.getContext(), IModelService.class.getName(), null);
			serviceTracker.waitForService(5000);
		}
		return elexisDataSourceStatus;
	}
	
	private static void persistDBConnection() throws IOException, JAXBException{
		if (connection != null) {
			try (OutputStream fos =
				Files.newOutputStream(connectionConfigPath, StandardOpenOption.CREATE)) {
				connection.marshall(fos);
			}
		}
	}
	
}
