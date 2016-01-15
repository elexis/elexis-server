package info.elexis.server.core.service;

import static info.elexis.server.core.constants.RestPathConstants.*;

import java.io.Serializable;
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
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private Logger log = LoggerFactory.getLogger(HTTPService.class);

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response getStatus() {
		long millis = new Date().getTime() - Application.getStarttime().getTime();
		String result = "Uptime: " + String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(millis),
				TimeUnit.MILLISECONDS.toSeconds(millis)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
		return Response.ok(result).build();
	}

	@GET
	@Path(LOGIN + "/{userid}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getSessionId(@PathParam("userid") String userid, @HeaderParam("encoded") String oPassword) {
		// TODO we should encode the IP into the session
		Optional<String> password = Optional.ofNullable(oPassword);
		if (password.isPresent()) {
			Optional<Serializable> sessionId = HTTPAuthHandler.getSessionId(userid, password.get());
			if (sessionId.isPresent()) {
				return Response.ok(sessionId.get().toString()).build();
			} else {
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}
		}
		return Response.status(Response.Status.BAD_REQUEST).build();
	}

	@GET
	@Path(HALT)
	@RolesAllowed("admin")
	public Response haltApplication() {
		Application.getInstance().shutdown();
		return Response.ok().build();
	}

	@GET
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
	@Path(SCHEDULER)
	@Produces(MediaType.APPLICATION_XML)
	public Response getSchedulerStatus() {
		SchedulerStatus schedulerStatus = Scheduler.INSTANCE.getSchedulerStatus();
		return Response.ok(schedulerStatus).build();
	}

	@GET
	@Path("/{subResources:.*}")
	public Response getInESPluginBundle(@PathParam("subResources") String subResources) {
		log.info("Unhandled GET on " + subResources);
		// consider access rights, call registered resources
		return Response.noContent().build();
	}
}
