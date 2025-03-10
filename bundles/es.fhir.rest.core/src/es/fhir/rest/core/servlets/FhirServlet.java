package es.fhir.rest.core.servlets;

import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import es.fhir.rest.core.resources.ServerCapabilityStatementProvider;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(name = "fhir", urlPatterns = { "/*" })
public class FhirServlet extends RestfulServer {

	private static Logger logger = LoggerFactory.getLogger(FhirServlet.class);

	private static final long serialVersionUID = -7883474501188868259L;

	public FhirServlet() {
		super(FhirContext.forR4());
		setServerName("Elexis-Server FHIR");
		setServerVersion("3.13");

		/*
		 * Register our resource providers
		 */
		FhirServletResourceCollector.getProviders().forEach(provider -> {
			logger.debug("Binding provider " + provider.getClass().getName());
			registerProvider(provider);
		});

		registerProvider(FhirServletResourceCollector.getPlainResourceProvider());

		setServerConformanceProvider(new ServerCapabilityStatementProvider(this));

		/*
		 * This server interceptor causes the server to return nicely formatter and
		 * coloured responses instead of plain JSON/XML if the request is coming from a
		 * browser window. It is optional, but can be nice for testing.
		 */
		registerInterceptor(new ResponseHighlighterInterceptor());

		// https://stackoverflow.com/questions/9117030/jul-to-slf4j-bridge
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		java.util.logging.Logger.getLogger("").setLevel(Level.INFO);

		/*
		 * This interceptor is used to generate a new log line (via SLF4j) for each
		 * incoming request.
		 */
		LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
		registerInterceptor(loggingInterceptor);
		loggingInterceptor.setMessageFormat(
				"REQ ${requestHeader.user-agent}@${remoteAddr} ${operationType} ${idOrResourceName} [${requestParameters}] [${requestBodyFhir}]");
		loggingInterceptor.setErrorMessageFormat(
				"REQ_ERR ${requestHeader.user-agent}@${remoteAddr} ${operationType} ${idOrResourceName} [${requestParameters}] - ${exceptionMessage} [${requestBodyFhir}]");

	}
}
