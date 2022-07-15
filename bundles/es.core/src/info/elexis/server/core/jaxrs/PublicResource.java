package info.elexis.server.core.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.osgi.service.component.annotations.Component;

import info.elexis.server.core.Application;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = {
	"system"
})
@Path("public")
@Component(service = PublicResource.class, immediate = true)
public class PublicResource {
	
	@GET
	@Path("uptime")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(nickname = "uptime", value = "show how long system has been running")
	public String getUptime(){
		return Application.uptime();
	}
	
}
