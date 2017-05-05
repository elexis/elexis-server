package info.elexis.server.core.service;

import static info.elexis.server.core.constants.RestPathConstants.BASE_URL_CORE;
import static info.elexis.server.core.constants.RestPathConstants.HALT;
import static info.elexis.server.core.constants.RestPathConstants.RESTART;
import static info.elexis.server.core.constants.RestPathConstants.SCHEDULER;
import static info.elexis.server.core.constants.RestPathConstants.SCHEDULER_LAUNCH;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
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

import info.elexis.server.core.Application;
import info.elexis.server.core.scheduler.SchedulerService;
import info.elexis.server.core.scheduler.SchedulerStatus;

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
	@Path(HALT)
	@RolesAllowed("admin")
	public Response haltApplication() {
		Application.shutdown(false);
		return Response.ok().build();
	}

	@GET
	@Path(RESTART)
	@RolesAllowed("admin")
	public Response restartApplication() {
		Application.restart(false);
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
		if (launched) {
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
