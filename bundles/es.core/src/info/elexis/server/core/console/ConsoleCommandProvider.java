package info.elexis.server.core.console;

import java.net.URL;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import info.elexis.server.core.internal.Application;
import info.elexis.server.core.scheduler.SchedulerService;

@Component(service = CommandProvider.class, immediate = true)
public class ConsoleCommandProvider implements CommandProvider {

	public void _es(CommandInterpreter ci) {
		final String argument = ci.nextArgument();
		if (argument == null) {
			System.out.println(getHelp());
			return;
		}
		try {
			switch (argument.toLowerCase()) {
			case "system":
				System.out.println(system(ci));
				return;
			case "scheduler":
				System.out.println(scheduler(ci));
				return;
			default:
				System.out.println(getHelp());
				return;
			}
		} catch (Exception e) {
			LoggerFactory.getLogger(ConsoleCommandProvider.class).error("Execution error on argument " + argument, e);
		}
	}

	private String system(CommandInterpreter ci) {
		final String argument = ci.nextArgument();
		if (argument == null) {
			return "Usage: es system (halt [force] | restart [force] | status | logTestError | reloadLogConfig)";
		}

		final String fv = ci.nextArgument();
		boolean force = fv != null && "force".equals(fv);

		switch (argument) {
		case "halt":
			Application.getInstance().shutdown(force);
			return "OK";
		case "restart":
			Application.getInstance().restart(force);
			return "OK";
		case "status":
			return Application.getStatus();
		case "logTestError":
			LoggerFactory.getLogger(ConsoleCommandProvider.class).error("TEST {}", "ERROR");
			return "SENT";
		case "reloadLogConfig":
			ConsoleCommandProvider.reloadLogger();
			return "OK";
		}

		return getHelp();
	}

	private String scheduler(CommandInterpreter ci) {
		final String argument = ci.nextArgument();
		if (argument == null) {
			return "Usage: es scheduler (launch taskId | deschedule taskId | status )";
		}
		final String taskIdArg = ci.nextArgument();

		switch (argument) {
		case "launch":
			if (taskIdArg == null) {
				return "ERR missing taskId";
			}
			boolean launched = SchedulerService.launchTask(taskIdArg);
			return (launched) ? "Launched " + argument : "Failed";
		case "deschedule":
			if (taskIdArg == null) {
				return "ERR missing taskId";
			}
			return Boolean.toString(SchedulerService.descheduleTask(taskIdArg));
		case "status":
			return SchedulerService.getSchedulerStatus().toString();
		default:
			break;
		}
		return "OK";
	}

	@Override
	public String getHelp() {
		return "Usage: es (system | scheduler)";
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
