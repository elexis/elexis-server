package info.elexis.server.core.connector.elexis.jpa.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

public class ElexisPoolingDataSource extends PoolingDataSource {
	
	public ElexisPoolingDataSource(GenericObjectPool<?> gp){
		super(gp);
	}
	
	@Override
	public Connection getConnection(String uname, String passwd) throws SQLException{
		return getConnection();
	}
}
