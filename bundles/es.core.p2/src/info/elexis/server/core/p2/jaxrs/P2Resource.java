package info.elexis.server.core.p2.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import info.elexis.server.core.p2.IProvisioner;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = { "p2" })
@Path("p2")
@Component(service = P2Resource.class, immediate = true)
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class P2Resource {

	@Reference
	private IProvisioner provisioner;
	
	@GET
	@Path("updates")
	@ApiOperation(nickname = "checkUpdate", value = "check for available updates")
	public Response checkUpdates() {
//		Update[] possibleUpdates = ProvisioningHelper.getPossibleUpdates();
//		List<String> resultList = Arrays.asList(possibleUpdates).stream().map(Update::toString)
//				.collect(Collectors.toList());
		return Response.ok(null).build();
	}

	@POST
	@Path("updates")
	@ApiOperation(nickname = "executeUpdate", value = "check for available updates")
	public Response executeUpdates() {
//		IStatus updateAllFeatures = ProvisioningHelper.updateAllFeatures();
//		if (updateAllFeatures.isOK()) {
//			return Response.ok().build();
//		}
//		return Response.ok().build();
		return null;
	}

	@GET
	@Path("repositories")
	public Response listRepositories(@QueryParam("filter") String filter) {
//		return HTTPServiceHelper.doRepositoryList(filter);
		return null;
	}

//	@GET
//	@Path("/repositories/add")
//	public Response addRepository(@QueryParam("location") String location) {
//		Optional<String> locStr = Optional.ofNullable(location);
//		if (locStr.isPresent()) {
//			return HTTPServiceHelper.doRepositoryAdd(locStr.get());
//		}
//		return Response.status(Response.Status.BAD_REQUEST).build();
//	}

//	@DELETE
//	@Path("/repositories/remove")
//	public Response handleRemove(@QueryParam("location") String location) {
//		Optional<String> locStr = Optional.ofNullable(location);
//		if (locStr.isPresent()) {
//			return HTTPServiceHelper.doRepositoryRemove(locStr.get());
//		}
//		return Response.status(Response.Status.BAD_REQUEST).build();
//	}
	
}
