package info.elexis.server.core.jaxrs;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.utils.CoreUtil;
import info.elexis.jaxrs.service.JaxrsResource;
import info.elexis.server.core.Application;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path("system")
@Component
public class SystemResource implements JaxrsResource {

	@GET
	@Path("log")
	@Produces(MediaType.TEXT_PLAIN)
	@Operation(summary = "download todays log file")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(implementation = File.class))) })
	public Response getLogFileOfToday() {
		File logFile = CoreUtil.getElexisServerHomeDirectory().resolve("logs/elexis-server.log").toFile();
		if (logFile.exists() && logFile.canRead()) {
			ResponseBuilder response = Response.ok(logFile);
			response.header("Content-Disposition", "attachment;filename=" + logFile.getName() + ".txt");
			return response.build();
		}
		return Response.serverError().build();
	}

	@POST
	@Operation(summary = "perform a system restart")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "422", description = "restart was vetoed, see message for reason") })
	public Response performSystemRestart() {
		String veto = Application.restart(false);
		if (veto != null) {
			return Response.status(422).type(MediaType.TEXT_PLAIN).entity(veto).build();
		}
		return Response.ok().build();
	}

}
