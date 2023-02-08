package es.fhir.rest.core.resources;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Subscription.SubscriptionChannelComponent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.PreconditionFailedException;
import ch.elexis.core.common.CatchingRunnable;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerException;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IAppointmentService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.status.StatusUtil;
import ch.elexis.core.utils.CoreUtil;
import es.fhir.rest.core.resources.util.SubscriptionResourceUtil;

/**
 * @see http://hl7.org/fhir/subscription.html
 */
@Component
public class SubscriptionResourceProvider implements IFhirResourceProvider<Subscription, Identifiable> {

	private Logger logger;

	private List<Subscription> activeSubscriptions;

	private ResourceProviderUtil resourceProviderUtil;
	private SubscriptionResourceUtil subscriptionResourceUtil;
	private ScheduledExecutorService scheduledExecutorService;

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference
	private IFhirTransformerRegistry transformerRegistry;

	@Reference
	private IAppointmentService appointmentService;

	@Activate
	public void activate() {
		logger = LoggerFactory.getLogger(getClass());

		resourceProviderUtil = new ResourceProviderUtil();
		subscriptionResourceUtil = new SubscriptionResourceUtil(transformerRegistry, appointmentService);
		activeSubscriptions = Collections.synchronizedList(new ArrayList<>());

		checkEnableSubscriptions(); // TODO persist subscriptions?
	}

	@Deactivate
	public void deactivate() {
		if (scheduledExecutorService != null) {
			scheduledExecutorService.shutdown();
		}
	}

	private void checkEnableSubscriptions() {
		if (!activeSubscriptions.isEmpty() && scheduledExecutorService == null) {
			scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
			long delay = CoreUtil.isTestMode() ? 1 : 5;
			CatchingRunnable catchingRunnable = new CatchingRunnable(new SubscriptionRunnable());
			scheduledExecutorService.scheduleWithFixedDelay(catchingRunnable, delay, delay, TimeUnit.SECONDS);
		}
	}

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Subscription.class;
	}

	@Override
	public IFhirTransformer<Subscription, Identifiable> getTransformer() {
		return null;
	}

	@Create
	public MethodOutcome create(@ResourceParam Subscription subscription) {

		MethodOutcome outcome = new MethodOutcome();
		IFhirTransformerException ex = null;

		String criteria = subscription.getCriteria();
		if (StringUtils.isBlank(criteria) || !StringUtils.startsWith(criteria, "Appointment")) {
			// only supports Appointments
			ex = new IFhirTransformerException("WARNING", "only appointments are currently supported", 0);
		}

		SubscriptionChannelComponent channel = subscription.getChannel();
		if (channel.getType() != Subscription.SubscriptionChannelType.RESTHOOK) {
			// not supported
			ex = new IFhirTransformerException("WARNING", "channel-type not supported", 0);
		}

		String endpoint = channel.getEndpoint();
		try {
			new URL(endpoint);
		} catch (MalformedURLException e) {
			ex = new IFhirTransformerException("WARNING", "invalid url for endpoint: " + e.getMessage(), 0);
		}

		if (ex != null) {
			OperationOutcome opOutcome = resourceProviderUtil.generateOperationOutcome(ex);
			throw new PreconditionFailedException(ex.getMessage(), opOutcome);
		}

		subscription.setId(UUID.randomUUID().toString());
		subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
		// subscription starts now
		subscription.getMeta().setLastUpdated(new Date());

		activeSubscriptions.add(subscription);
		logger.info("[{}] Subscription added", subscription.getId());
		checkEnableSubscriptions();

		// return the created subscription
		outcome.setCreated(true);
		outcome.setId(subscription.getIdElement());
		outcome.setResource(subscription);
		return outcome;
	}

	@Delete
	public void delete(@IdParam IdType theId) {
		for (Iterator<Subscription> iterator = activeSubscriptions.iterator(); iterator.hasNext();) {
			Subscription subscription = iterator.next();
			if (theId.getIdPart().equals(subscription.getId())) {
				logger.info("[{}] Subscription removed", theId.getValue());
				iterator.remove();
			}
		}
	}

	@Read
	public Subscription read(@IdParam IdType theId) {
		for (Iterator<Subscription> iterator = activeSubscriptions.iterator(); iterator.hasNext();) {
			Subscription subscription = iterator.next();
			if (theId.getIdPart().equals(subscription.getId())) {
				return subscription;
			}
		}
		return null;
	}

	@Search
	public List<Subscription> search() {
		return activeSubscriptions;
	}

	private class SubscriptionRunnable implements Runnable {

		@Override
		public void run() {

			for (Iterator<Subscription> iterator = activeSubscriptions.iterator(); iterator.hasNext();) {
				Subscription subscription = iterator.next();

				Date lastUpdated = subscription.getMeta().getLastUpdated();
				IStatus status = Status.OK_STATUS;

				if (StringUtils.startsWith(subscription.getCriteria(), "Appointment")) {
					IQuery<IAppointment> query = coreModelService.getQuery(IAppointment.class, true, true);
					query.and(ModelPackage.Literals.IDENTIFIABLE__LASTUPDATE, COMPARATOR.GREATER,
							lastUpdated.getTime());
					List<IAppointment> queryResult = query.execute();
					if (queryResult.size() > 0) {
						// TODO automatic unregister if multiple fails?
						status = subscriptionResourceUtil.handleNotification(queryResult, subscription);
					}
				}

				if (status.isOK()) {
					subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
					subscription.getMeta().setLastUpdated(new Date());
				} else {
					if (Status.CANCEL == status.getSeverity()) {
						StatusUtil.logStatus("Subscription [" + subscription.getId() + "]", logger, status);
						iterator.remove();
					} else {
						subscription.setStatus(Subscription.SubscriptionStatus.ERROR);
						StatusUtil.logStatus("Subscription [" + subscription.getId() + "]", logger, status);
					}
				}

			}
		}

	}

}
