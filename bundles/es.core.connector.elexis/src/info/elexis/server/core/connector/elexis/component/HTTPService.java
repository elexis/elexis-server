package info.elexis.server.core.connector.elexis.component;

import static info.elexis.server.core.constants.RestPathConstants.ELEXIS_CONNECTION;

import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Component;

import info.elexis.server.core.connector.elexis.internal.DBConnection;
import info.elexis.server.core.connector.elexis.internal.ElexisDBConnection;
import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Config;

@Component(service = HTTPService.class, immediate = true)
@Path("/elexis/connector/")
public class HTTPService {


	
	@POST
	@Path(ELEXIS_CONNECTION)
	@RolesAllowed("admin")
	public Response setData(DBConnection connection) {
		ElexisDBConnection.setConnection(connection);
		return Response.ok().build();
	}

	@GET
	@Path(ELEXIS_CONNECTION)
	@Produces(MediaType.APPLICATION_XML)
	public Response getElexisDBConnectionStatus() {
		Optional<DBConnection> connectiono = ElexisDBConnection.getConnection();
		DBConnection dbc = new DBConnection();
		if (connectiono.isPresent()) {
			dbc = connectiono.get();
			dbc.password = "[PASSWORD REMOVED]";
		}
		return Response.ok(dbc).build();
	}
	
	@GET
	public Response getDBInformation() {
		Config cDBV = ElexisEntityManager.em().find(Config.class, "dbversion");
		if (cDBV == null) {
			return Response.serverError().build();
		}

		String dbv = cDBV.getWert();
		String elVersion = ElexisEntityManager.em().find(Config.class, "ElexisVersion").getWert();
		String created = ElexisEntityManager.em().find(Config.class, "created").getWert();
		String statusInfo = "Connected with Elexis " + elVersion + ", DB " + dbv + " (" + created + ")";

		return Response.ok(statusInfo).build();
	}
}
