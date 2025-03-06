package info.elexis.server.core.jaxrs;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.jaxrs.JaxrsResource;
import info.elexis.server.core.Application;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


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
