package es.fhir.rest.core.resources.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Subscription.SubscriptionChannelComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.client.interceptor.SimpleRequestHeaderInterceptor;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.agenda.Area;
import ch.elexis.core.services.IAppointmentService;
import es.fhir.rest.core.websocket.r4.SubscriptionWebSocketConnections;

public class SubscriptionResourceUtil {

	private Logger logger;
	private HttpClient httpClient;
	private FhirContext fhirContext;
	private IFhirTransformerRegistry transformerRegistry;
	private IAppointmentService appointmentService;

	public SubscriptionResourceUtil(IFhirTransformerRegistry transformerRegistry,
			IAppointmentService appointmentService) {
		this.logger = LoggerFactory.getLogger(getClass());
		this.transformerRegistry = transformerRegistry;
		this.appointmentService = appointmentService;
	}

	private HttpClient getHttpClient() {
		if (httpClient == null) {
			httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2)
					.connectTimeout(Duration.ofSeconds(2)).build();
		}
		return httpClient;
	}

	private FhirContext getFhirContext() {
		if (fhirContext == null) {
			fhirContext = FhirContext.forR4();
			// we don't expect a full-fledged FHIR server as comm partner ...
			fhirContext.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
			fhirContext.getRestfulClientFactory().setConnectTimeout(2 * 1000);
			fhirContext.getRestfulClientFactory().setSocketTimeout(2 * 1000);
		}
		return fhirContext;
	}

	/**
	 * 
	 * @param queryResult  all appointments to notify - in ASCENDING order of
	 *                     updates!!
	 * @param subscription
	 * @return
	 */
	public IStatus handleNotification(List<IAppointment> queryResult, Subscription subscription) {

		SubscriptionChannelComponent channel = subscription.getChannel();
		if (StringUtils.equals("application/fhir+json", channel.getPayload())) {
			// requests notification per id
			for (IAppointment iAppointment : queryResult) {
				IStatus status = sendPayloadUpdateNotification(subscription, iAppointment);
				if (!status.isOK()) {
					return status;
				}
				subscription.getMeta().setLastUpdated(new Date(iAppointment.getLastupdate()));
			}
			return Status.OK_STATUS;
		} else {
			subscription.getMeta().setLastUpdated(new Date(queryResult.get(queryResult.size() - 1).getLastupdate()));
			return sendGenericNotification(subscription);
		}
	}

	private IStatus sendGenericNotification(Subscription subscription) {
		SubscriptionChannelComponent channel = subscription.getChannel();
		if (channel.getType() == Subscription.SubscriptionChannelType.RESTHOOK) {
			return sendGenericNotificationRestHook(subscription);
		} else if (channel.getType() == Subscription.SubscriptionChannelType.WEBSOCKET) {
			return sendGenericNotificationWebSocket(subscription);
		}
		return Status.error("Unsupported channel type " + channel.getType());
	}

	private IStatus sendGenericNotificationWebSocket(Subscription subscription) {
		return SubscriptionWebSocketConnections.sendPing(subscription.getId());
	}

	private IStatus sendGenericNotificationRestHook(Subscription subscription) {
		SubscriptionChannelComponent channel = subscription.getChannel();
		Builder requestBuilder = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(""))
				.uri(URI.create(channel.getEndpoint())).setHeader("User-Agent", "ES FHIR Subscription Notifier");

		List<StringType> headers = channel.getHeader();
		headers.stream().forEach(header -> {
			String[] split = header.getValueAsString().split(":");
			if (split.length == 2) {
				requestBuilder.setHeader(split[0].trim(), split[1].trim());
			}
		});

		HttpRequest request = requestBuilder.build();
		CompletableFuture<HttpResponse<String>> response = getHttpClient().sendAsync(request,
				HttpResponse.BodyHandlers.ofString());
		try {
			logger.debug("[{}] Generic HTTP POST [{}]", subscription.getId(), channel.getEndpoint());
			Integer statusCode = response.thenApply(HttpResponse::statusCode).get(2, TimeUnit.SECONDS);
			if (statusCode == 200) {
				return Status.OK_STATUS;
			} else {
				return Status.warning("Http StatusCode " + statusCode);
			}

		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			return Status.warning(e.getMessage(), e);
		}
	}

	private IStatus sendPayloadUpdateNotification(Subscription subscription, IAppointment appointment) {
		String endpoint = subscription.getChannel().getEndpoint();

		Optional<Appointment> fhirObject = transformerRegistry.getTransformerFor(Appointment.class, IAppointment.class)
				.getFhirObject(appointment);

		if (fhirObject.isPresent()) {
			IGenericClient fhirClient = getFhirContext().newRestfulGenericClient(endpoint);
			subscription.getChannel().getHeader().forEach(header -> {
				SimpleRequestHeaderInterceptor simpleRequestHeaderInterceptor = new SimpleRequestHeaderInterceptor(
						header.asStringValue());
				fhirClient.registerInterceptor(simpleRequestHeaderInterceptor);
			});

			// It is not possible to directly add the schedule id to the Schedule object
			// we pass it using a header
			Area areaByNameOrId = appointmentService.getAreaByNameOrId(appointment.getSchedule());
			if (areaByNameOrId != null) {
				fhirClient.registerInterceptor(
						new SimpleRequestHeaderInterceptor("x-fhir-schedule-id", "Schedule/" + areaByNameOrId.getId()));
			}

			LoggingInterceptor loggingInterceptor = new LoggingInterceptor();

			loggingInterceptor.setLogRequestBody(true);
			loggingInterceptor.setLogRequestHeaders(true);
			fhirClient.registerInterceptor(loggingInterceptor);

			try {
				MethodOutcome methodOutcome;
				if (appointment.isDeleted()) {
					methodOutcome = fhirClient.delete().resource(fhirObject.get()).execute();
				} else {
					methodOutcome = fhirClient.update().resource(fhirObject.get()).execute();
				}

				if (checkUnsubscribeRequest(methodOutcome)) {
					return new Status(Status.CANCEL, "Subscription", 1, "Endpoint requests unsubscription", null);
				}

				return Status.OK_STATUS;
			} catch (BaseServerResponseException bsre) {
				return Status.error(bsre.getLocalizedMessage());
			}
		}
		return Status.error("Could not transform to fhir object");
	}

	/**
	 * Check if the subscription endpoint requests to be unsubscribed
	 * 
	 * @param methodOutcome
	 * @return
	 */
	private boolean checkUnsubscribeRequest(MethodOutcome methodOutcome) {
		List<String> unsubscribe = methodOutcome.getResponseHeaders().get("x-req-unsubscribe");
		if (unsubscribe != null && unsubscribe.size() > 0) {
			return Boolean.valueOf(unsubscribe.get(0));
		}
		return false;
	}

}
