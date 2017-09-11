package info.elexis.server.core.connector.elexis.jpa.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.eclipse.persistence.sessions.Session;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.Driver;

public class ElexisDataSourceUtil {
	
	public static Optional<DataSource> getDataSource(Session session){
		
		String driverName = (String) session.getProperties().get("javax.persistence.jdbc.driver");
		String username = (String) session.getProperties().get("javax.persistence.jdbc.user");
		String password = (String) session.getProperties().get("javax.persistence.jdbc.password");
		String connection = (String) session.getProperties().get("javax.persistence.jdbc.url");
		try {
			Driver driver = (Driver) Class.forName(driverName).newInstance();
			
			Properties properties = new Properties();
			properties.put("user", username);
			properties.put("password", password);
			properties.setProperty("useSSL", "false");
			properties.setProperty("autoReconnect", "true");
			
			ConnectionFactory connectionFactory =
				new DriverConnectionFactory(driver, connection,
					properties);
			
			GenericObjectPool<?> connectionPool = new GenericObjectPool<Connection>(null);
			connectionPool.setMaxActive(32);
			connectionPool.setMinIdle(2);
			connectionPool.setMaxWait(10000);
			connectionPool.setTestOnBorrow(true);
			
			new PoolableConnectionFactory(connectionFactory, connectionPool, null, "SELECT 1;",
				false, true);
			ElexisPoolingDataSource dataSource = new ElexisPoolingDataSource(connectionPool);
			
			// test establishing a connection
			Connection conn = dataSource.getConnection();
			conn.close();
			
			LoggerFactory.getLogger(ElexisDataSourceUtil.class)
				.info("db pool initialization success");
			
			session.getLogin().useExternalConnectionPooling();
			return Optional.of(dataSource);
		} catch (SQLException | InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			LoggerFactory.getLogger(ElexisDataSourceUtil.class).error("pool initialization error",
				e);
			return Optional.empty();
		}
		
	}
}
