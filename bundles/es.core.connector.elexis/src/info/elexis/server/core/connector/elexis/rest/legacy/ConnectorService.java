package info.elexis.server.core.connector.elexis.rest.legacy;

import java.util.Optional;

import javax.ws.rs.core.Response;

import org.eclipse.core.runtime.IStatus;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.server.IConnectorService;
import info.elexis.server.core.connector.elexis.common.ElexisDBConnection;

@Component(service = ConnectorService.class, immediate = true)
public class ConnectorService implements IConnectorService {

	public DBConnection getElexisDBConnectionStatus() {
		Optional<DBConnection> connectiono = ElexisDBConnection.getConnection();
		DBConnection dbc = new DBConnection();
		if (connectiono.isPresent()) {
			dbc = connectiono.get();
			dbc.password = "[PASSWORD REMOVED]";
		}
		return null;
	}

	@Override
	public Response getDBInformation() {
		IStatus dbi = ElexisDBConnection.getDatabaseInformation();
		return Response.ok(dbi.getMessage()).build();
	}

	@Override
	public ch.elexis.core.common.DBConnection getElexisDBConnection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response setElexisDBConnection(ch.elexis.core.common.DBConnection dbConnection) {
		// TODO Auto-generated method stub
		return null;
	}
}
