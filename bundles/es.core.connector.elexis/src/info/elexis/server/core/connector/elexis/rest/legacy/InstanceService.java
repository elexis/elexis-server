package info.elexis.server.core.connector.elexis.rest.legacy;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.common.InstanceStatus;
import ch.elexis.core.server.IInstanceService;

@Component(service = InstanceService.class, immediate = true)
public class InstanceService implements IInstanceService {

	@Context
	HttpServletRequest hsrRequest;

	@Override
	public Response updateStatus(InstanceStatus request) {
		info.elexis.server.core.connector.elexis.instances.InstanceService.updateInstanceStatus(request,
				hsrRequest.getRemoteAddr());
		return Response.ok().build();
	}

	@Override
	public Response getStatus() {
		List<InstanceStatus> statusList = info.elexis.server.core.connector.elexis.instances.InstanceService
				.getInstanceStatus();
		final GenericEntity<List<InstanceStatus>> list = new GenericEntity<List<InstanceStatus>>(statusList) {
		};
		return Response.ok(list).build();
	}

}
