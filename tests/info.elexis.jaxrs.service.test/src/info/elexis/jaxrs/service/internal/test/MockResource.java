package info.elexis.jaxrs.service.internal.test;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/test")
interface MockResource {

	@GET
	String getContent();

	@POST
	String postContent(String content);

	@PUT
	String putContent(String content);

	@DELETE
	String deleteContent();

	@GET
	@Path("/json")
	@Produces(MediaType.APPLICATION_JSON)
	MockElement getJson();

	@GET
	@Path("/xml")
	@Produces(MediaType.APPLICATION_XML)
	MockElement getXml();
}
