package info.elexis.server.core.p2.jaxrs;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.equinox.p2.operations.Update;
import org.osgi.service.component.annotations.Component;

import info.elexis.server.core.p2.internal.ProvisioningHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = { "p2" })
@Path("/p2")
@Component(service = UpdateResource.class, immediate = true)
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class UpdateResource {

	@GET
	@Path("updates/check")
	@ApiOperation(nickname = "checkUpdates", value = "check for available updates")
	public Response checkUpdates() {
		List<String> resultList = new ArrayList<>();
		Update[] possibleUpdates = ProvisioningHelper.getPossibleUpdates();
		if (possibleUpdates != null) {
			for (Update update : possibleUpdates) {
				resultList.add(update.toString());
			}
		}
		return Response.ok(resultList).build();
	}

}
