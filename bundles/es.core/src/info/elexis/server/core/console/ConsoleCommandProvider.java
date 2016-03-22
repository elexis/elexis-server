package info.elexis.server.core.console;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.internal.Application;
import info.elexis.server.core.scheduler.SchedulerService;

@Component(service = CommandProvider.class, immediate = true)
public class ConsoleCommandProvider implements CommandProvider {

	private Logger log = LoggerFactory.getLogger(ConsoleCommandProvider.class);

	public void _es(CommandInterpreter ci) {
		final String argument = ci.nextArgument();
		try {
			if (argument == null) {
				System.out.println(getHelp());
				return;
			}

			switch (argument.toLowerCase()) {
			case "status":
				System.out.println(showStatus(ci.nextArgument()));
				return;
			case "launch":
				System.out.println(launch(ci.nextArgument()));
				return;
			case "system":
				System.out.println(system(ci.nextArgument()));
				return;
			default:
				System.out.println(getHelp());
				return;
			}
		} catch (Exception e) {
			log.error("Execution error on argument " + argument, e);
		}
	}

	private String system(String argument) {
		if (argument == null) {
			return "Usage: es system (halt | restart)";
		}

		switch (argument) {
		case "halt":
			Application.getInstance().shutdown();
			return "OK";
		case "restart":
			Application.getInstance().restart();
			return "OK";
		}

		return getHelp();
	}

	private String launch(String argument) {
		if (argument == null) {
			return "Usage: es launch taskId";
		}
		boolean launched = SchedulerService.launchTask(argument);
		return (launched) ? "Launched " + argument : "Failed";
	}

	private String showStatus(final String argument) {
		if (argument == null) {
			return "Usage: es status (system | scheduler )";
		}
		switch (argument) {
		case "system":
			return Application.getStatus();
		case "scheduler":
			return SchedulerService.getSchedulerStatus().toString();
		default:
			break;
		}
		return getHelp();
	}

	@Override
	public String getHelp() {
		return "Usage: es (status | launch | system)";
	}

}
