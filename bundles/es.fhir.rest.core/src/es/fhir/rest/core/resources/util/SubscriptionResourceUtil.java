package es.fhir.rest.core.resources.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Subscription.SubscriptionChannelComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IAppointment;

public class SubscriptionResourceUtil {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private HttpClient httpClient;

	private HttpClient getHttpClient() {
		if (httpClient == null) {
			httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2)
					.connectTimeout(Duration.ofSeconds(2)).build();
		}
		return httpClient;
	}

	public IStatus handleNotification(List<IAppointment> queryResult, Subscription subscription) {

		SubscriptionChannelComponent channel = subscription.getChannel();
		if (channel.getType() == Subscription.SubscriptionChannelType.RESTHOOK) {

			HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(""))
					.uri(URI.create(channel.getEndpoint())).setHeader("User-Agent", "ES FHIR Subscription Notifier")
					.build();

			CompletableFuture<HttpResponse<String>> response = getHttpClient().sendAsync(request,
					HttpResponse.BodyHandlers.ofString());
			try {
				logger.debug("[{}] Sending HTTP POST to [{}]", subscription.getId(), channel.getEndpoint());
				Integer statusCode = response.thenApply(HttpResponse::statusCode).get(2, TimeUnit.SECONDS);
				if (statusCode == 200) {
					return Status.OK_STATUS;
				} else {
					return Status.warning("Http StatusCode " + statusCode);
				}

			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				return Status.warning(e.getMessage(), e);
			}

		} else {
			return Status.error("unsupported channel type");
		}

	}

}
