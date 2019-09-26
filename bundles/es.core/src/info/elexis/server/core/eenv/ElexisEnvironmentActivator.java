package info.elexis.server.core.eenv;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.services.IContextService;
import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

@Component
public class ElexisEnvironmentActivator {

	@Reference
	private IElexisEnvironmentService elexisEnvironmentService;

	@Reference
	private IContextService contextService;

	@Activate
	public void activate() {
		String hostname = elexisEnvironmentService.getHostname();
		activateRocketChatLogAppender(hostname);
	}

	private void activateRocketChatLogAppender(String hostname) {

		String rocketchatIntegrationToken = elexisEnvironmentService.getProperty("EE_RC_ES_INTEGRATION_WEBHOOK_TOKEN");
		String rocketchatIntegrationUrl = "https://" + hostname + "/chat/hooks/" + rocketchatIntegrationToken;

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
			
		} else {
			LoggerFactory.getLogger(getClass()).error("Could not get rocketchat appender from root logger");
		}
	}

}