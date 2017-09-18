package info.elexis.server.core.connector.elexis.common;

import java.util.Optional;

import javax.persistence.EntityManager;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.persistence.config.PersistenceUnitProperties;

import ch.elexis.core.common.DBConnection;
import es.core.connector.elexis.datasource.util.ElexisDBConnectionUtil;
import info.elexis.server.core.connector.elexis.internal.BundleConstants;
import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Config;

public class ElexisDBConnection {

	public static Optional<DBConnection> getConnection(){
		return ElexisDBConnectionUtil.getConnection();
	}
	
	public static boolean isTestMode(){
		return ElexisDBConnectionUtil.isTestMode();
	}

	public static IStatus getDatabaseInformation() {
		String statusInfo = getDatabaseInformationString();
		return new Status(Status.OK, BundleConstants.BUNDLE_ID, statusInfo);
	}

	public static String getDatabaseInformationString() {
		EntityManager entityManager = ElexisEntityManager.createEntityManager();
		if (entityManager == null) {
			return "Entity Manager is null.";
		}
		try {
			Config cDBV = entityManager.find(Config.class, "dbversion");
			if (cDBV == null) {
				return "Could not find dbversion entry in config table.";
			}

			String dbv = cDBV.getWert();
			String url = (String) entityManager.getProperties().get(PersistenceUnitProperties.JDBC_URL);
			String elVersion = entityManager.find(Config.class, "ElexisVersion").getWert();
			String created = entityManager.find(Config.class, "created").getWert();
			String statusInfo = "Elexis " + elVersion + " [" + url + "] DBv " + dbv + " (" + created + ")";
			return statusInfo;
		} finally {
			entityManager.close();
		}
	}
}
