package info.elexis.server.core.eenv;

import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IMessageTransporter;
import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

/**
 * This service is programmatically activated via
 * 
 * @see ElexisEnvironmentServiceActivator
 */
@Component
public class ElexisEnvironmentActivator {

	@Reference
	private IElexisEnvironmentService elexisEnvironmentService;

	@Reference
	private IContextService contextService;

	@Reference
	private IConfigService configService;

	private ServiceRegistration<IMessageTransporter> serviceRegistration;

	@Activate
	public void activate() {
		if (Boolean.valueOf(elexisEnvironmentService.getProperty("ENABLE_ROCKETCHAT"))) {
//			LoggerFactory.getLogger(getClass()).debug("Activating rocketchat support");
//			String rocketchatBaseUrl = elexisEnvironmentService.getRocketchatBaseUrl();
//			String rocketchatIntegrationToken = elexisEnvironmentService
//					.getProperty("EE_RC_ES_INTEGRATION_WEBHOOK_TOKEN");
//
//			String rocketchatIntegrationUrl = rocketchatBaseUrl + "/hooks/" + rocketchatIntegrationToken;
//
//			configureRocketchatMessageTransporter(rocketchatIntegrationUrl);
//			configureRocketchatIntegration(rocketchatBaseUrl, rocketchatIntegrationToken);
		} else {
			LoggerFactory.getLogger(getClass()).debug("Rocketchat support not activated");
		}
	}

	@Deactivate
	public void deactivate() {
		if (serviceRegistration != null) {
			serviceRegistration.unregister();
		}
	}

	private void configureRocketchatMessageTransporter(String rocketchatIntegrationUrl) {
		if (StringUtils.isNotEmpty(rocketchatIntegrationUrl)) {
			RocketchatMessageTransporter rocketchatMessageTransporter = new RocketchatMessageTransporter(
					rocketchatIntegrationUrl);
			serviceRegistration = FrameworkUtil.getBundle(RocketchatMessageTransporter.class).getBundleContext()
					.registerService(IMessageTransporter.class, rocketchatMessageTransporter, null);
		}
	}

	private void configureRocketchatIntegration(String rocketchatBaseUrl, String rocketchatIntegrationToken) {
		String rocketchatIntegrationUrl = rocketchatBaseUrl + "/hooks/" + rocketchatIntegrationToken;

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		Logger slf4jRootLogger = lc.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) slf4jRootLogger;

		Appender<ILoggingEvent> rocketchatAppender = rootLogger.getAppender("rocketchatAppender");
		if (rocketchatAppender != null) {
			/**
			 * Logs events >= Level.WARN
			 */
			rocketchatAppender.getContext().putProperty("integrationUrl", rocketchatIntegrationUrl);
			rocketchatAppender.getContext().putProperty("identification", contextService.getStationIdentifier());
			rootLogger.detachAppender(rocketchatAppender);

			AsyncAppender asyncAppender = new AsyncAppender();
			asyncAppender.setContext(rootLogger.getLoggerContext());
			asyncAppender.addAppender(rocketchatAppender);
			asyncAppender.start();
			rootLogger.addAppender(asyncAppender);
			LoggerFactory.getLogger(getClass()).info("Configured rocketchatAppender to [{}]", rocketchatIntegrationUrl);
		} else {
			LoggerFactory.getLogger(getClass()).error("Could not get rocketchatAppender from root logger");
		}
	}

}