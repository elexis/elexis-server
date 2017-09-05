package at.medevit.logback.mattermost.test;

import java.io.IOException;
import java.net.MalformedURLException;

import at.medevit.logback.mattermost.IntegrationPostHandler;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;

public class IntegrationPostHandlerTest {

	private Logger testLogger = new LoggerContext().getLogger(IntegrationPostHandler.class);

	public static final String CHANNEL_URL = "INSERT CHANNEL URL";

	public static void main(String[] args) throws MalformedURLException, IOException {

		ILoggingEvent ile = new IntegrationPostHandlerTest().createLoggingEvent();

		System.out.println(new IntegrationPostHandler(ile, null, false).post(CHANNEL_URL));
	}

	public ILoggingEvent createLoggingEvent() {
		return createNestedLoggingEvent();
	}

	private ILoggingEvent createNestedLoggingEvent() {
		return new LoggingEvent("fqcn", testLogger, Level.WARN, "TEST WARNING MESSAGE", new Throwable("Diagnosis"),
				(Object[]) null);
	}

}
