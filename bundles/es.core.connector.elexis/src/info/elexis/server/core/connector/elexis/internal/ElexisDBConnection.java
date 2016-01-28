package info.elexis.server.core.connector.elexis.internal;

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

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Config;
import info.elexis.server.core.util.CoreUtil;

public class ElexisDBConnection {

	private static DBConnection connection;
	private static Path connectionConfigPath;

	private static Logger log = LoggerFactory.getLogger(ElexisDBConnection.class);

	static {
		connectionConfigPath = CoreUtil.getHomeDirectory().resolve("elexis-connection.xml");
		if (connectionConfigPath.toFile().exists()) {
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

	public static void setConnection(DBConnection connection) {
		ElexisDBConnection.connection = connection;

		try (OutputStream os = Files.newOutputStream(connectionConfigPath, StandardOpenOption.WRITE);) {
			connection.marshall(os);
		} catch (IOException | JAXBException e) {
			log.error("Error marshalling connection", e);
		}

		ElexisEntityManager.initializeEntityManager();
	}

	public static IStatus getDatabaseInformation() {
		EntityManager em = ElexisEntityManager.em();
		if (em == null) {
			return new Status(Status.ERROR, BundleConstants.BUNDLE_ID, "Entity Manager is null.");
		}
		Config cDBV = em.find(Config.class, "dbversion");
		if (cDBV == null) {
			return new Status(Status.ERROR, BundleConstants.BUNDLE_ID,
					"Could not find dbversion entry in config table.");
		}

		String dbv = cDBV.getWert();
		String elVersion = ElexisEntityManager.em().find(Config.class, "ElexisVersion").getWert();
		String created = ElexisEntityManager.em().find(Config.class, "created").getWert();
		String statusInfo = "Connected with Elexis " + elVersion + ", DB " + dbv + " (" + created + ")";

		return new Status(Status.OK, BundleConstants.BUNDLE_ID, statusInfo);
	}
}
