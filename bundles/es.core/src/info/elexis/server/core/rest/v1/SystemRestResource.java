package info.elexis.server.core.rest.v1;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.osgi.service.component.annotations.Component;

@Path("system/v1")
@Component(service = SystemRestResource.class, immediate = true)
public class SystemRestResource {

	@GET
	@Path("protected")
	@Produces(MediaType.TEXT_PLAIN)
	@RequiresRoles("esadmin")
	public String getStatusPlaintext() {
		return "PROTECTED RESOURCE";
	}
	
}
