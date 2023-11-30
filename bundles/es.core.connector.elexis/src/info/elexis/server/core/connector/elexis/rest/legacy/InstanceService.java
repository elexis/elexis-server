package info.elexis.server.core.connector.elexis.rest.legacy;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.common.InstanceStatus;
import ch.elexis.core.server.IInstanceService;
import info.elexis.jaxrs.service.JaxrsResource;

@Component
public class InstanceService implements IInstanceService, JaxrsResource {

	@Context
	HttpServletRequest hsrRequest;

	@Override
	public Response updateStatus(InstanceStatus request) {
		String remoteAddr = hsrRequest.getHeader("X-Real-IP");
		if (remoteAddr == null) {
			hsrRequest.getRemoteAddr();
		}
		info.elexis.server.core.connector.elexis.internal.services.InstanceService.updateInstanceStatus(request,
				remoteAddr);
		return Response.ok().build();
	}

	@Override
	public Response getStatus() {
		List<InstanceStatus> statusList = info.elexis.server.core.connector.elexis.internal.services.InstanceService
				.getInstanceStatus();
		final GenericEntity<List<InstanceStatus>> list = new GenericEntity<List<InstanceStatus>>(statusList) {
		};
		return Response.ok(list).build();
	}

}
