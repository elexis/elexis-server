package info.elexis.server.fhir.rest.core.resources;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

@Path("subscription-test-endpoint")
@Component(service = SubscriptionResourceTestEndpointProvider.class, immediate = true)
public class SubscriptionResourceTestEndpointProvider {

	private static int callCounter = 0;

	@POST
	@Path("post")
	public Response post() {
		LoggerFactory.getLogger(getClass()).info("Received POST call");
		SubscriptionResourceTestEndpointProvider.increment();
		return Response.ok().build();
	}

	private static synchronized void increment() {
		callCounter++;
	}

	public static int getCallCounter() {
		return callCounter;
	}

}
