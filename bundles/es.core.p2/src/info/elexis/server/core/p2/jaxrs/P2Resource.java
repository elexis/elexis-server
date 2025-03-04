package info.elexis.server.core.p2.jaxrs;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.operations.Update;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import info.elexis.jaxrs.service.JaxrsResource;
import info.elexis.server.core.p2.IProvisioner;
import info.elexis.server.core.p2.internal.RepoInfo;
import info.elexis.server.core.rest.ResponseStatusUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("p2")
@Component
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class P2Resource implements JaxrsResource {

	@Reference
	private IProvisioner provisioner;

	@GET
	@Path("updates")
	@Operation(summary = "check for available updates")
	public Response checkUpdates() {
		Collection<Update> availableUpdates = provisioner.getAvailableUpdates();
		List<String> resultList = availableUpdates.stream().map(Update::toString).collect(Collectors.toList());
		return Response.ok(resultList).build();
	}

	@POST
	@Path("updates")
	@Operation(summary = "execute update")
	public Response executeUpdates() {
		Collection<Update> availableUpdates = provisioner.getAvailableUpdates();
		IStatus status = provisioner.update(availableUpdates, new NullProgressMonitor());
		return ResponseStatusUtil.convert(status);
	}

	@GET
	@Path("repositories")
	@Operation(summary = "list the used p2 repositories")
	public Response listRepositories() {
		RepoInfo repositoryInfo = provisioner.getRepositoryInfo();
		return Response.ok(repositoryInfo).build();
	}

}
