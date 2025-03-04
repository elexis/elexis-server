package info.elexis.jaxrs.service.internal.test;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
