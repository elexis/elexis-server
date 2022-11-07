package info.elexis.server.fhir.rest.core.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

@Path("subscription-test-endpoint")
@Component(service = SubscriptionResourceTestEndpointProvider.class, immediate = true)
public class SubscriptionResourceTestEndpointProvider {

	private static int postCallCounter = 0;
	private static int putCallCounter = 0;
	private static int deleteCallCounter = 0;

	@POST
	@Path("post")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response post() {
		LoggerFactory.getLogger(getClass()).info("POST");
		postCallCounter++;
		return Response.ok().build();
	}

	@PUT
	@Path("Appointment/{id}")
	public Response put(@Context HttpHeaders headers, String appointment) {
		LoggerFactory.getLogger(getClass()).info("PUT: " + appointment);
		putCallCounter++;
		return Response.ok().build();
	}

	@DELETE
	@Path("Appointment/{id}")
	public Response delete() {
		LoggerFactory.getLogger(getClass()).info("DELETE");
		deleteCallCounter++;
		return Response.ok().build();
	}

	public static int getPostCallCounter() {
		return postCallCounter;
	}

	public static int getPutCallCounter() {
		return putCallCounter;
	}

	public static int getDeleteCallCounter() {
		return deleteCallCounter;
	}

}
