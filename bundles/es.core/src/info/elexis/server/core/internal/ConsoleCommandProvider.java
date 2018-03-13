package info.elexis.server.core.internal;

import java.net.URL;
import java.util.Iterator;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import info.elexis.server.core.Application;
import info.elexis.server.core.console.AbstractConsoleCommandProvider;

@Component(service = CommandProvider.class, immediate = true)
public class ConsoleCommandProvider extends AbstractConsoleCommandProvider {

	public void _es(CommandInterpreter ci) {
		executeCommand(ci);
	}

	public String __system() {
		return getHelp(1);
	}

	public String __system_halt(Iterator<String> args) {
		boolean force = false;
		if (args.hasNext()) {
			force = "force".equals(args.next());
		}
		String vetoReason = Application.shutdown(force);
		return (vetoReason != null) ? vetoReason : ok();
	}

	public String __system_restart(Iterator<String> args) {
		boolean force = false;
		if (args.hasNext()) {
			force = "force".equals(args.next());
		}
		String vetoReason = Application.restart(force);
		return (vetoReason != null) ? vetoReason : ok();
	}

	public String __system_status() {
		return Application.uptime();
	}

	public void __system_logTestError() {
		LoggerFactory.getLogger(ConsoleCommandProvider.class).error("TEST {}", "ERROR", new Throwable("Diagnosis"));
		ok();
	}

	public void __system_reloadLogConfig() {
		ConsoleCommandProvider.reloadLogger();
		ok();
	}

	public String __system_security() {
		return getHelp(2);
	}

	public String __system_security_user() {
		return getHelp(3);
	}

	/**
	 * Reconfigures the system logger by reloading all log configuration files
	 * 
	 * @see <a href=
	 *      "http://stackoverflow.com/questions/9320133/how-do-i-programmatically
	 *      -tell-logback-to-reload-configuration">stackoverflow</a>
	 */
	public static void reloadLogger() {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

		ContextInitializer ci = new ContextInitializer(loggerContext);
		URL url = ci.findURLOfDefaultConfigurationFile(true);

		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(loggerContext);
			loggerContext.reset();
			configurator.doConfigure(url);
		} catch (JoranException je) {
			// StatusPrinter will handle this
		}
		StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
	}

}
