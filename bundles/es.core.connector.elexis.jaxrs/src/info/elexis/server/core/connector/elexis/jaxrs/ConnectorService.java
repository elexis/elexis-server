package info.elexis.server.core.connector.elexis.jaxrs;

import java.util.Optional;

import javax.ws.rs.core.Response;

import org.eclipse.core.runtime.IStatus;
import org.osgi.service.component.annotations.Component;

import info.elexis.server.core.connector.elexis.common.DBConnection;
import info.elexis.server.core.connector.elexis.common.ElexisDBConnection;
import info.elexis.server.elexis.common.jaxrs.IConnectorService;

@Component(service = ConnectorService.class, immediate = true)
public class ConnectorService implements IConnectorService {

	@Override
	public Response getElexisDBConnectionStatus() {
		Optional<DBConnection> connectiono = ElexisDBConnection.getConnection();
		DBConnection dbc = new DBConnection();
		if (connectiono.isPresent()) {
			dbc = connectiono.get();
			dbc.password = "[PASSWORD REMOVED]";
		}
		return Response.ok(dbc).build();
	}

	@Override
	public Response getDBInformation() {
		IStatus dbi = ElexisDBConnection.getDatabaseInformation();
		return Response.ok(dbi.getMessage()).build();
	}
}
