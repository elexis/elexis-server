package info.elexis.server.core.service;

import static info.elexis.server.core.constants.RestPathConstants.*;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.osgi.service.component.annotations.Component;

import info.elexis.server.core.connector.elexis.ElexisDBConnection;
import info.elexis.server.core.extension.DBConnection;
import info.elexis.server.core.internal.Application;
import info.elexis.server.core.scheduler.Scheduler;
import info.elexis.server.core.scheduler.SchedulerStatus;
import info.elexis.server.core.security.HTTPAuthHandler;

@Component(service = HTTPService.class, immediate = true)
@Path(BASE_URL_CORE)
public class HTTPService {
	@Context
	UriInfo uriInfo;
	@Context
	HttpServletRequest request;

	@GET
	public Response getStatus() {
		long millis = new Date().getTime() - Application.getStarttime().getTime();
		String result = "Uptime: " + String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(millis),
				TimeUnit.MILLISECONDS.toSeconds(millis)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
		return Response.ok(result).build();
	}

	@GET
	@Path("/login/{userid}")
	public Response getSessionId(@PathParam("userid") String userid, @HeaderParam("encoded") String oPassword) {
		// TODO we should encode the IP into the session
		Optional<String> password = Optional.ofNullable(oPassword);
		if (password.isPresent()) {
			String id = HTTPAuthHandler.getSessionId(userid, password.get()).toString();
			return Response.ok(id).build();
		}
		return Response.status(Response.Status.BAD_REQUEST).build();
	}

	@POST
	@Path(HALT)
	@RolesAllowed("admin")
	public Response haltApplication() {
		Application.getInstance().shutdown();
		return Response.ok().build();
	}

	@POST
	@Path(RESTART)
	@RolesAllowed("admin")
	public Response restartApplication() {
		Application.getInstance().restart();
		return Response.ok().build();
	}

	@POST
	@Path(ELEXIS_CONNECTION)
	@RolesAllowed("admin")
	public Response setData(DBConnection connection) {
		ElexisDBConnection.updateConnection(connection);
		return Response.ok().build();
	}

	@GET
	@Path(ELEXIS_CONNECTION)
	public Response getElexisDBConnectionStatus() {
		Optional<DBConnection> connectiono = ElexisDBConnection.getConnection();
		if (connectiono.isPresent()) {
			DBConnection dbc = connectiono.get();
			dbc.password = "removed";
			return Response.ok(dbc).build();
		}
		return Response.status(Response.Status.NOT_FOUND).build();
	}

	@GET
	@Path(SCHEDULER)
	public Response getSchedulerStatus() {
		SchedulerStatus schedulerStatus = Scheduler.INSTANCE.getSchedulerStatus();
		return Response.ok(schedulerStatus).build();
	}

	// @GET
	// @Path(CONNECTOR_ELEXIS_CONNECTORS)
	// @Produces(MediaType.APPLICATION_JSON)
	// public Response getElexisDBConnectors() {
	// List<IElexisConnector> elexisConnectors =
	// ElexisConnectorManager.getElexisConnectors();
	// List<String> collect = elexisConnectors.stream().map(e ->
	// e.getClass().getName()).collect(Collectors.toList());
	// GenericEntity<List<String>> entity = new
	// GenericEntity<List<String>>(collect) {
	// };
	// return Response.ok(entity).build();
	// }
	//
	// @POST
	// @Path(CONNECTOR_ELEXIS_CONNECTORS)
	// @RolesAllowed("admin")
	// public Response setDefaultElexisDBConnector(String className) {
	// if (className.isEmpty()) {
	// ElexisConnectorManager.setSystemConnector(null);
	// } else {
	// Optional<IElexisConnector> connector =
	// ElexisConnectorManager.getElexisConnectorByClassName(className);
	// if (!connector.isPresent()) {
	// return Response.status(Status.NOT_FOUND).build();
	// }
	// ElexisConnectorManager.setSystemConnector(connector.get());
	// }
	// return Response.ok().build();
	// }

	@GET
	@Path("/{subResources:.*}")
	public Response getInESPluginBundle(@PathParam("subResources") String subResources) {
		// consider access rights, call registered resources
		System.out.println(subResources);
		return null;
	}
}
