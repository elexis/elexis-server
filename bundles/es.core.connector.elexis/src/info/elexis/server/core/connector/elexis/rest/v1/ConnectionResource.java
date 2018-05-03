package info.elexis.server.core.connector.elexis.rest.v1;

import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;
import org.eclipse.core.runtime.IStatus;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.common.DBConnection;
import info.elexis.server.core.connector.elexis.common.ElexisDBConnection;
import info.elexis.server.core.connector.elexis.datasource.util.ElexisDBConnectionUtil;
import info.elexis.server.core.rest.ResponseStatusUtil;
import info.elexis.server.core.security.RestPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = { "elexis-connector/connection" })
@Path("/elexis/connector/v1")
@Component(service = ConnectionResource.class, immediate = true)
public class ConnectionResource {

	@GET
	@Path("status")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "database status information")
	public String getDatabaseStatusInformation() {
		return ElexisDBConnection.getDatabaseInformationString();
	}

	@GET
	@Path("connection")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@ApiOperation(value = "retrieve the elexis-database-connection")
	public DBConnection getDBConnection() {
		Optional<DBConnection> connection = ElexisDBConnectionUtil.getConnection();
		if (connection.isPresent()) {
			SecurityUtils.getSubject().checkPermission(RestPermission.ADMIN_READ);
		}
		return connection.isPresent() ? connection.get() : null;
	}

	@POST
	@Path("connection")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@ApiOperation(value = "set the elexis-database-connection")
	public Response setDBConnection(DBConnection dbConnection) {
		if (ElexisDBConnectionUtil.getConnection().isPresent()) {
			SecurityUtils.getSubject().checkPermission(RestPermission.ADMIN_WRITE);
		}
		IStatus status = ElexisDBConnectionUtil.setConnection(dbConnection);
		return ResponseStatusUtil.convert(status);
	}


}
