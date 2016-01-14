package info.elexis.server.core.connector.elexis.jpa.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Component;

import info.elexis.server.core.connector.elexis.jpa.manager.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Config;

@Component(service = HTTPService.class, immediate = true)
@Path("/elexis/connector/")
public class HTTPService {

	@GET
	public Response getDBInformation() {
		Config cDBV = ElexisEntityManager.em().find(Config.class, "dbversion");
		if (cDBV == null) {
			return Response.serverError().build();
		}

		String dbv = cDBV.getWert();
		String elVersion = ElexisEntityManager.em().find(Config.class, "ElexisVersion").getWert();
		String created = ElexisEntityManager.em().find(Config.class, "created").getWert();
		String statusInfo = "Connected with Elexis " + elVersion + ", DB " + dbv + " (" + created + ")";

		return Response.ok(statusInfo).build();
	}
}
