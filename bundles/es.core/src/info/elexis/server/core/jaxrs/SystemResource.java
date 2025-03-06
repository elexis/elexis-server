package info.elexis.server.core.jaxrs;

import java.io.File;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.jaxrs.JaxrsResource;
import ch.elexis.core.utils.CoreUtil;
import info.elexis.server.core.Application;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

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
