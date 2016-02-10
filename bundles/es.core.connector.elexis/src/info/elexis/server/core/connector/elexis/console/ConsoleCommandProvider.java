package info.elexis.server.core.connector.elexis.console;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Component;

import info.elexis.server.core.common.StatusUtil;
import info.elexis.server.core.connector.elexis.common.ElexisDBConnection;
import info.elexis.server.core.connector.elexis.services.LockService;

@Component(service = CommandProvider.class, immediate = true)
public class ConsoleCommandProvider implements CommandProvider {

	public void _es_elc(CommandInterpreter ci) throws Exception {
		final String argument = ci.nextArgument();
		if (argument == null) {
			System.out.println(getHelp());
			return;
		}
		switch (argument) {
		case "connectionStatus":
			IStatus dbi = ElexisDBConnection.getDatabaseInformation();
			StatusUtil.printStatus(System.out, dbi);
			break;
		case "listLocks":
			System.out.println(LockService.consoleListLocks());
			break;
		case "clearAllLocks":
			LockService.clearAllLocks();
			break;
		default:
			break;
		}
	}

	@Override
	public String getHelp() {
		return "Usage: es_elc (connectionStatus | listLocks | clearAllLocks !!WARN!!)";
	}
}
