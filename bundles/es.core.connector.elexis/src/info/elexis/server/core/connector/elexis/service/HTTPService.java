package info.elexis.server.core.connector.elexis.service;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import ch.elexis.core.data.util.DBConnection;
import info.elexis.server.core.connector.elexis.ElexisConnection;


@Path("/connector/elexis")
public class HTTPService {
	
	@GET
	public String getStatus() {
		String ret = "Not connected";
		
//		DBConnection dbc = ElexisConnection.getConnection();
//		if(dbc != null) {
//			boolean connectionSuccessful = false;
//			
//			if (dbc.rdbmsType != null && dbc.connectionString != null) {
//				JdbcLink link = new JdbcLink(
//						dbc.rdbmsType.driverName,
//						dbc.connectionString, dbc.rdbmsType.dbType);
//				if (link.connect(dbc.username,
//						dbc.password)) {
//					connectionSuccessful = PersistentObject.connect(link);
//					if (connectionSuccessful) {
//						String dbv = CoreHub.globalCfg.get("dbversion", null);
//						String elVersion = CoreHub.globalCfg.get("ElexisVersion",
//								null);
//						String created = CoreHub.globalCfg.get("created", null);
//						ret = "Elexis " + elVersion + ", DB " + dbv + " ("
//								+ created + ")";
//					}
//					PersistentObject.disconnect();
//				} else {
//					connectionSuccessful = false;
//				}
//			}
//
//			if (connectionSuccessful) {
//				ret = "[OK] " + ret;
//			} else {
//				ret = "[ERROR]";
//			}
//		} 
		
		return ret;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@RolesAllowed("admin")
	public Response setData(DBConnection connection) {
		ElexisConnection.setConnection(connection);
		return Response.status(Status.OK).build();
	}
	
	
	
}
