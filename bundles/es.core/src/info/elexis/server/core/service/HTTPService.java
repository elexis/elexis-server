package info.elexis.server.core.service;

import static info.elexis.server.core.constants.RestPathConstants.*;

import java.io.Serializable;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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

import info.elexis.server.core.internal.Application;
import info.elexis.server.core.scheduler.SchedulerService;
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
		return Response.ok(Application.getStatus()).build();
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
		Application.getInstance().shutdown(false);
		return Response.ok().build();
	}

	@GET
	@Path(RESTART)
	@RolesAllowed("admin")
	public Response restartApplication() {
		Application.getInstance().restart(false);
		return Response.ok().build();
	}

	@GET
	@Path(SCHEDULER)
	@Produces(MediaType.APPLICATION_XML)
	public Response getSchedulerStatus() {
		SchedulerStatus schedulerStatus = SchedulerService.getSchedulerStatus();
		return Response.ok(schedulerStatus).build();
	}
	
	@GET
	@Path(SCHEDULER_LAUNCH + "/{taskId}")
	@RolesAllowed("admin")
	public Response startScheduledTask(@PathParam("taskId") String taskId) {
		boolean launched = SchedulerService.launchTask(taskId);
		if(launched) {
			return Response.ok().build();
		}
		return Response.status(Response.Status.NOT_FOUND).build();
	}

	@GET
	@Path("/{subResources:.*}")
	public Response getInESPluginBundle(@PathParam("subResources") String subResources) {
		log.info("Unhandled GET on " + subResources);
		// consider access rights, call registered resources
		return Response.noContent().build();
	}
}
