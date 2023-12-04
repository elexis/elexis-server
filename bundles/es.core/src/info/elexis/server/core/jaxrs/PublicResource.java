package info.elexis.server.core.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.osgi.service.component.annotations.Component;

import info.elexis.jaxrs.service.JaxrsResource;
import info.elexis.server.core.Application;
import io.swagger.v3.oas.annotations.Operation;


@Path("public")
@Component
public class PublicResource implements JaxrsResource {
	
	@GET
	@Path("uptime")
	@Produces(MediaType.TEXT_PLAIN)
	@Operation(summary = "show how long system has been running")
	public String getUptime(){
		return Application.uptime();
	}
	
}
