package info.elexis.server.core.p2.internal;

import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.osgi.service.component.annotations.Component;

@Component(service = HTTPService.class, immediate = true)
@Path("/p2")
public class HTTPService {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	
}
