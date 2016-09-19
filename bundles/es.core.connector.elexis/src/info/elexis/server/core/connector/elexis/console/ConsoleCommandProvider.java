package info.elexis.server.core.connector.elexis.console;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.InstanceStatus;
import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.status.StatusUtil;
import info.elexis.server.core.connector.elexis.common.ElexisDBConnection;
import info.elexis.server.core.connector.elexis.instances.InstanceService;
import info.elexis.server.core.connector.elexis.instances.ServerHeldInstanceStatus;
import info.elexis.server.core.connector.elexis.services.LockService;

@Component(service = CommandProvider.class, immediate = true)
public class ConsoleCommandProvider implements CommandProvider {

	private Logger log = LoggerFactory.getLogger(ConsoleCommandProvider.class);

	public void _es_elc(CommandInterpreter ci) {
		final String argument = ci.nextArgument();
		try {
			if (argument == null) {
				System.out.println(getHelp());
				return;
			}
			switch (argument) {
			case "connectionStatus":
				IStatus dbi = ElexisDBConnection.getDatabaseInformation();
				StatusUtil.printStatus(System.out, dbi);
				break;
			case "listInstances":
				listInstances(ci);
				break;
			case "listLocks":
				listLocks(ci);
				break;
			case "clearAllLocks":
				LockService.clearAllLocks();
				break;
			default:
				break;
			}
		} catch (Exception e) {
			log.error("Execution error on argument " + argument, e);
		}

	}

	private void listInstances(CommandInterpreter ci) {
		ci.println("======= " + LocalDateTime.now() + " ==== server uuid [" + LockService.getSystemuuid() + "]");
		List<ServerHeldInstanceStatus> status = InstanceService.getInstanceStatus();
		for (int i = 0; i < status.size(); i++) {
			ServerHeldInstanceStatus s = status.get(i);
			InstanceStatus inst = s.getInstanceStatus();
			ci.println(i + ") " + s.getRemoteAddress() + " " + inst);
			LocalDateTime now = LocalDateTime.now();
			long until = s.getLastUpdate().until(now, ChronoUnit.SECONDS);
			if (until > 60) {
				ci.println("\tFS:" + s.getFirstSeen() + " LU:" + s.getLastUpdate() + " (!!!!)");
			} else {
				ci.println("\tFS:" + s.getFirstSeen() + " LU:" + s.getLastUpdate());
			}
		}
	}

	private void listLocks(CommandInterpreter ci) {
		ci.println("======= " + LocalDateTime.now() + " ==== server uuid [" + LockService.getSystemuuid() + "]");
		for (LockInfo lockInfo : LockService.getAllLockInfo()) {
			ci.println(lockInfo.getUser() + "@" + lockInfo.getElementType() + "::" + lockInfo.getElementId() + "\t"
					+ lockInfo.getCreationDate() + "\t[" + lockInfo.getSystemUuid() + "]");
		}
	}

	@Override
	public String getHelp() {
		return "Usage: es_elc (connectionStatus | listInstances | listLocks | clearAllLocks !!WARN!!)";
	}
}
