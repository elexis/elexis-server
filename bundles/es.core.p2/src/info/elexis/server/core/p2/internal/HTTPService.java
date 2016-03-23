package info.elexis.server.core.p2.internal;

import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Component;

@Component(service = HTTPService.class, immediate = true)
@Path("/p2")
@RolesAllowed("admin")
public class HTTPService {
	@Context
	private HttpServletRequest req;

	@GET
	@Path("/repositories")
	public Response listRepositories(@QueryParam("filter") String filter) {
		return HTTPServiceHelper.doRepositoryList(filter);
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

	@GET
	@Path("/repositories/remove")
	public Response handleRemove(@QueryParam("location") String location) {
		Optional<String> locStr = Optional.ofNullable(location);
		if (locStr.isPresent()) {
			return HTTPServiceHelper.doRepositoryRemove(locStr.get());
		}
		return Response.status(Response.Status.BAD_REQUEST).build();
	}

	@GET
	@Path("/update")
	public Response performUpdate() {
		return HTTPServiceHelper.doUpdateAllFeatures();
	}

}
