package info.elexis.server.core.p2.jaxrs;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.operations.Update;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import info.elexis.server.core.p2.IProvisioner;
import info.elexis.server.core.p2.internal.RepoInfo;
import info.elexis.server.core.rest.ResponseStatusUtil;
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
		Collection<Update> availableUpdates = provisioner.getAvailableUpdates();
		List<String> resultList = availableUpdates.stream().map(Update::toString).collect(Collectors.toList());
		return Response.ok(resultList).build();
	}

	@POST
	@Path("updates")
	@ApiOperation(nickname = "executeUpdate", value = "execute update")
	public Response executeUpdates() {
		Collection<Update> availableUpdates = provisioner.getAvailableUpdates();
		IStatus status = provisioner.update(availableUpdates, new NullProgressMonitor());
		return ResponseStatusUtil.convert(status);
	}

	@GET
	@Path("repositories")
	@ApiOperation(nickname = "listRepositories", value = "list the used p2 repositories")
	public Response listRepositories() {
		RepoInfo repositoryInfo = provisioner.getRepositoryInfo();
		return Response.ok(repositoryInfo).build();
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
