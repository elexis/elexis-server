package info.elexis.server.findings.fhir.jpa.model.service.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.DBConnection;

public class DbInitializer {

	private static Logger logger = LoggerFactory.getLogger(DbInitializer.class);

	private DBConnection dbConnection;

	public DbInitializer(DBConnection dbConnection) {
		this.dbConnection = dbConnection;
	}

	public void init() {
		Optional<Connection> connectionOpt = getJdbcConnection(dbConnection);
		if (connectionOpt.isPresent()) {
			Connection jdbcConnection = connectionOpt.get();
			try {
				if (!tableExists(jdbcConnection, "CH_ELEXIS_CORE_FINDINGS_ENCOUNTER")) {
					executeDbScript(jdbcConnection, "/rsc/findings_encounter.sql");
				}
				if (!tableExists(jdbcConnection, "CH_ELEXIS_CORE_FINDINGS_CONDITION")) {
					executeDbScript(jdbcConnection, "/rsc/findings_condition.sql");
				}
				if (!tableExists(jdbcConnection, "CH_ELEXIS_CORE_FINDINGS_PROCEDUREREQUEST")) {
					executeDbScript(jdbcConnection, "/rsc/findings_procedurereq.sql");
				}
				if (!tableExists(jdbcConnection, "CH_ELEXIS_CORE_FINDINGS_OBSERVATION")) {
					executeDbScript(jdbcConnection, "/rsc/findings_observation.sql");
				}
				if (!tableExists(jdbcConnection, "CH_ELEXIS_CORE_FINDINGS_FAMILYMEMBERHISTORY")) {
					executeDbScript(jdbcConnection, "/rsc/findings_familymemberhistory.sql");
				}
				if (!tableExists(jdbcConnection, "CH_ELEXIS_CORE_FINDINGS_ALLERGYINTOLERANCE")) {
					executeDbScript(jdbcConnection, "/rsc/findings_allergyintolerance.sql");
				}
			} catch (IOException | SQLException e) {
				logger.error("Faild to run sql script on database.", e);
				return;
			} finally {
				try {
					jdbcConnection.close();
				} catch (SQLException e) {
					// ignore
				}
			}
		}
	}

	private void executeDbScript(Connection jdbcConnection, String path) throws IOException, SQLException {
		try (InputStream is = DbInitializer.class.getResourceAsStream(path)) {
			ScriptRunner runner = new ScriptRunner(jdbcConnection, true, true);
			runner.runScript(new InputStreamReader(is));
		}
	}

	private Optional<Connection> getJdbcConnection(DBConnection connection) {
		try {
			Driver driver = (Driver) Class.forName(connection.rdbmsType.driverName).newInstance();

			Properties properties = new Properties();
			properties.put("user", connection.username);
			properties.put("password", connection.password);

			Connection jdbcConnection = driver.connect(connection.connectionString, properties);

			return Optional.of(jdbcConnection);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			logger.error("Faild to create connection to database.", e);
			return Optional.empty();
		}
	}

	private boolean tableExists(Connection jdbcConnection, String tablename) throws SQLException {
		DatabaseMetaData meta = jdbcConnection.getMetaData();
		String[] onlyTables = { "TABLE" };
		ResultSet rs = meta.getTables(null, null, "%", onlyTables);
		if (rs != null) {
			while (rs.next()) {
				// DatabaseMetaData#getTables() specifies TABLE_NAME is in
				// column 3
				if (rs.getString(3).equalsIgnoreCase(tablename)) {
					return true;
				}
			}
		}
		return false;
	}
}
