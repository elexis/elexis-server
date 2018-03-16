package info.elexis.server.core.rest.v1;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.osgi.service.component.annotations.Component;

import info.elexis.server.core.Application;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

@Api(value = "/system", tags = { "system" })
@Path("system/v1")
@Component(service = RestResource.class, immediate = true)
public class RestResource {

	@GET
	@Path("uptime")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "show how long system has been running")
	public String uptime() {
		return Application.uptime();
	}

	@GET
	@Path("protected")
	@Produces(MediaType.TEXT_PLAIN)
	@RequiresRoles("esadmin")
	@ApiOperation(value = "Get the system status in plaintext", authorizations = {
			@Authorization(value = "esoauth", scopes = {
					@AuthorizationScope(scope = "esadmin", description = "bla") }) })
	/**
	 * @return
	 */
	@Deprecated
	public String getStatusPlaintext() {
		return "PROTECTED RESOURCE";
	}

}
