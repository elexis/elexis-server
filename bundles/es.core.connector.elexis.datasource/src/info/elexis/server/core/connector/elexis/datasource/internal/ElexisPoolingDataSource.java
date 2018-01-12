package info.elexis.server.core.connector.elexis.datasource.internal;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.DBConnection;
import info.elexis.server.core.connector.elexis.datasource.util.ElexisDBConnectionUtil;

public class ElexisPoolingDataSource extends PoolingDataSource implements DataSource {

	private static Logger log = LoggerFactory.getLogger(ElexisPoolingDataSource.class);

	public IStatus activate() {
		Optional<DBConnection> dbConnection = ElexisDBConnectionUtil.getConnection();
		if (dbConnection.isPresent()) {
			ObjectPool<Connection> connectionPool = createConnectionPool(dbConnection.get());
			if (connectionPool != null) {
				setPool(connectionPool);
				try (Connection conn = getConnection()) {
					return new Status(Status.OK, Activator.BUNDLE_ID, "db pool initialization success");
				} catch (SQLException e) {
					return new Status(Status.ERROR, Activator.BUNDLE_ID, "db pool initialization error", e);
				}
			} else {
				return new Status(Status.ERROR, Activator.BUNDLE_ID,
						"db pool initialization failed - no connection pool used");
			}
		}
		return new Status(Status.ERROR, Activator.BUNDLE_ID, "No database connection available.");
	}

	private ObjectPool<Connection> createConnectionPool(DBConnection dbConnection) {
		String driverName = dbConnection.rdbmsType.driverName;
		String username = dbConnection.username;
		String password = dbConnection.password;
		String connection = dbConnection.connectionString;
		try {
			Driver driver = (Driver) Class.forName(driverName).newInstance();

			Properties properties = new Properties();
			properties.put("user", username);
			properties.put("password", password);

			ConnectionFactory connectionFactory = new DriverConnectionFactory(driver, connection, properties);

			GenericObjectPool<Connection> connectionPool = new GenericObjectPool<>(null);
			connectionPool.setMaxActive(32);
			connectionPool.setMinIdle(2);
			connectionPool.setMaxWait(10000);
			connectionPool.setTestOnBorrow(true);

			new PoolableConnectionFactory(connectionFactory, connectionPool, null, "SELECT 1;", false, true);
			return connectionPool;

		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			log.error("pool initialization error", e);
			return null;
		}
	}

	@Override
	public Connection getConnection(String uname, String passwd) throws SQLException {
		return getConnection();
	}

	@Override
	public Connection getConnection() throws SQLException {
		return super.getConnection();
	}
}