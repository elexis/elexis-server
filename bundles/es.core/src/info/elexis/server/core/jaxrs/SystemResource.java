package info.elexis.server.core.jaxrs;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.osgi.service.component.annotations.Component;

import info.elexis.server.core.Application;
import info.elexis.server.core.common.util.CoreUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

@Api(tags = { "system" })
@Path("system")
@Component(service = SystemResource.class, immediate = true)
public class SystemResource {

	@GET
	@Path("uptime")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(nickname = "uptime", value = "show how long system has been running")
	public String getUptime() {
		return Application.uptime();
	}

	@GET
	@Path("log")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(nickname = "downloadTodaysLogAsTxt", value = "download todays log file", authorizations = {
			@Authorization(value = "esoauth") })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "", response = File.class) })
	@RequiresRoles("esadmin")
	public Response getLogFileOfToday() {
		File logFile = CoreUtil.getHomeDirectory().resolve("logs/elexis-server.log").toFile();
		if (logFile.exists() && logFile.canRead()) {
			ResponseBuilder response = Response.ok((Object) logFile);
			response.header("Content-Disposition", "attachment;filename=" + logFile.getName() + ".txt");
			return response.build();
		}
		return Response.serverError().build();
	}

	@POST
	@ApiOperation(nickname = "restartSystem", value = "perform a system restart")
	@ApiResponses(value = { @ApiResponse(code = 422, message = "restart was vetoed, see message for reason") })
	public Response performSystemRestart() {
		String veto = Application.restart(false);
		if (veto != null) {
			return Response.status(422).type(MediaType.TEXT_PLAIN).entity(veto).build();
		}
		return Response.ok().build();
	}

}
