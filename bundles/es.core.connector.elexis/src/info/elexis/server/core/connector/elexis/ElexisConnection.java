package info.elexis.server.core.connector.elexis;

import ch.elexis.core.data.util.DBConnection;
import info.elexis.server.core.connector.elexis.internal.ConfigFileManager;

public class ElexisConnection {

	private static DBConnection connection;

	public static DBConnection getConnection() {
		if (connection == null) {
			connection = ConfigFileManager.loadStoredDBConnection();
		}

		return connection;
	}

	public static void setConnection(DBConnection connection) {
		ElexisConnection.connection = connection;
		ConfigFileManager.storeDBConnection(connection);
		
		// update jpa?
	}

}
