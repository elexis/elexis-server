package info.elexis.server.core.connector.elexis.rest;

import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;
import org.eclipse.core.runtime.IStatus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.services.IElexisDataSource;
import info.elexis.server.core.connector.elexis.common.ElexisDBConnection;
import info.elexis.server.core.connector.elexis.common.ElexisDBConnectionUtil;
import info.elexis.server.core.security.RestPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = {
	"elexis-connector"
})
@Path("/elexis-connector/connection")
@Component(service = ConnectionResource.class, immediate = true)
@Produces({
	MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
public class ConnectionResource {
	
	@Reference
	private IElexisDataSource elexisDataSource;
	
	@GET
	@Path("status")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "database status information")
	public String getDatabaseStatusInformation(){
		return ElexisDBConnection.getDatabaseInformationString();
	}
	
	@GET
	@ApiOperation(value = "retrieve the elexis-database-connection")
	public DBConnection getDBConnection(){
		Optional<DBConnection> connection = ElexisDBConnectionUtil.getConnection();
		if (connection.isPresent()) {
			SecurityUtils.getSubject().checkPermission(RestPermission.ADMIN_READ);
		}
		if (!connection.isPresent()) {
			throw new WebApplicationException(Response.Status.NO_CONTENT);
		}
		return connection.get();
	}
	
	@POST
	@Consumes({
		MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON
	})
	@ApiOperation(value = "set the elexis-database-connection")
	@ApiResponses(value = {
		@ApiResponse(code = 200, message = "success"),
		@ApiResponse(code = 422, message = "error connecting to requested database, see error string"),
		@ApiResponse(code = 401, message = "a connection was already set, and the request does lack the right to change it")
	})
	public Response setDBConnection(DBConnection dbConnection){
		if (ElexisDBConnectionUtil.getConnection().isPresent()) {
			SecurityUtils.getSubject().checkPermission(RestPermission.ADMIN_WRITE);
		}
		IStatus status = ElexisDBConnectionUtil.setConnection(elexisDataSource, dbConnection);
		if (status.isOK()) {
			return Response.ok().build();
		}
		return Response.status(422).type(MediaType.TEXT_PLAIN).entity(status.getMessage()).build();
	}
}
