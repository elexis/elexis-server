package info.elexis.server.fhir.rest.core.resources;

import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
