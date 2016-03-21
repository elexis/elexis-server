package info.elexis.server.core.connector.elexis.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.xml.bind.JAXBException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.common.util.CoreUtil;
import info.elexis.server.core.connector.elexis.common.DBConnection.DBType;
import info.elexis.server.core.connector.elexis.internal.BundleConstants;
import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Config;


public class ElexisDBConnection {

	private static DBConnection connection;
	private static Path connectionConfigPath;

	private static Logger log = LoggerFactory.getLogger(ElexisDBConnection.class);

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
				InputStream is = Files.newInputStream(connectionConfigPath, StandardOpenOption.READ);
				connection = DBConnection.unmarshall(is);
				is.close();
				log.info("Initialized elexis connection from " + connectionConfigPath.toAbsolutePath());
				setConnection(connection);
			} catch (IOException | JAXBException e) {
				log.warn("Error opening " + connectionConfigPath.toAbsolutePath(), e);
			}
		}
	}

	public static Optional<DBConnection> getConnection() {
		return Optional.ofNullable(connection);
	}

	private static void setConnection(DBConnection connection) {
		ElexisDBConnection.connection = connection;

		try (OutputStream os = Files.newOutputStream(connectionConfigPath, StandardOpenOption.WRITE);) {
			connection.marshall(os);
		} catch (IOException | JAXBException e) {
			log.error("Error marshalling connection", e);
		}

//		ElexisEntityManager.initializeEntityManager();
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

	public static IStatus getDatabaseInformation() {
		EntityManager em = ElexisEntityManager.createEntityManager();
		if (em == null) {
			return new Status(Status.ERROR, BundleConstants.BUNDLE_ID, "Entity Manager is null.");
		}
		Config cDBV = em.find(Config.class, "dbversion");
		if (cDBV == null) {
			return new Status(Status.ERROR, BundleConstants.BUNDLE_ID,
					"Could not find dbversion entry in config table.");
		}

		String dbv = cDBV.getWert();
		String elVersion = ElexisEntityManager.createEntityManager().find(Config.class, "ElexisVersion").getWert();
		String created = ElexisEntityManager.createEntityManager().find(Config.class, "created").getWert();
		String statusInfo = "Connected with Elexis " + elVersion + ", DB " + dbv + " (" + created + ")";

		return new Status(Status.OK, BundleConstants.BUNDLE_ID, statusInfo);
	}
}
