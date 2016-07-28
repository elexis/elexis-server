package info.elexis.server.core.connector.elexis.jpa.test;

import info.elexis.server.core.connector.elexis.common.DBConnection;
import info.elexis.server.core.connector.elexis.common.DBConnection.DBType;

public class TestDatabase {

	public static DBConnection getDBConnection() {
		DBConnection connection = new DBConnection();
		connection.connectionString = "jdbc:h2:mem:elexisTest;DB_CLOSE_DELAY=-1";
		connection.rdbmsType = DBType.H2;
		connection.username = "sa";
		connection.password = "";
		return connection;
	}

}
