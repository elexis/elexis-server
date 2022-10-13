package info.elexis.server.core.internal;

import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ch.elexis.core.console.AbstractConsoleCommandProvider;
import ch.elexis.core.console.CmdAdvisor;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import info.elexis.server.core.Application;

@Component(service = CommandProvider.class, immediate = true)
public class ConsoleCommandProvider extends AbstractConsoleCommandProvider {

	@Activate
	public void activate() {
		register(this.getClass());
	}

	@CmdAdvisor(description = "system related commands")
	public void _system(CommandInterpreter ci) {
		executeCommand("system", ci);
	}

	@CmdAdvisor(description = "halt the system")
	public String __system_halt(Iterator<String> args) {
		boolean force = false;
		if (args.hasNext()) {
			force = "force".equals(args.next());
		}
		String vetoReason = Application.shutdown(force);
		return (vetoReason != null) ? vetoReason : ok();
	}

	@CmdAdvisor(description = "reboot/restart the system")
	public String __system_restart(Iterator<String> args) {
		boolean force = false;
		if (args.hasNext()) {
			force = "force".equals(args.next());
		}
		String vetoReason = Application.restart(force);
		return (vetoReason != null) ? vetoReason : ok();
	}

	@CmdAdvisor(description = "system uptime")
	public String __system_status() {
		return Application.uptime();
	}

	@CmdAdvisor(description = "test log an error (e.g. to test push notification)")
	public void __system_logTestError() {
		LoggerFactory.getLogger(ConsoleCommandProvider.class).error("TEST {}", "ERROR", new Throwable("Diagnosis"));
		ok();
	}

	@CmdAdvisor(description = "reload the logging configuration (e.g. after modification of logback-addition.xml)")
	public void __system_reloadLogConfig() {
		ConsoleCommandProvider.reloadLogger();
		ok();
	}

	@CmdAdvisor(description = "List the available printers")
	public void __system_listPrinters() {
		PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
		for (PrintService printer : services) {
			ci.println(printer.getName() + " " + Arrays.toString(printer.getAttributes().toArray()));
		}
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
