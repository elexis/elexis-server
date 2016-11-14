package info.elexis.server.core.connector.elexis.console;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.common.InstanceStatus;
import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.status.StatusUtil;
import info.elexis.server.core.connector.elexis.common.ElexisDBConnection;
import info.elexis.server.core.connector.elexis.instances.InstanceService;
import info.elexis.server.core.connector.elexis.services.LockService;
import info.elexis.server.core.console.AbstractConsoleCommandProvider;

@Component(service = CommandProvider.class, immediate = true)
public class ConsoleCommandProvider extends AbstractConsoleCommandProvider {

	public void _es_elc(CommandInterpreter ci) {
		executeCommand(ci);
		final String argument = ci.nextArgument();
		try {
			if (argument == null) {
				System.out.println(getHelp());
				return;
			}
			switch (argument) {

			case "clearAllLocks":

				break;
			default:
				break;
			}
		} catch (Exception e) {
			log.error("Execution error on argument " + argument, e);
		}
	}

	public void __connectionStatus() {
		IStatus dbi = ElexisDBConnection.getDatabaseInformation();
		StatusUtil.printStatus(System.out, dbi);
	}

	public void __listInstances() {
		ci.println("======= " + LocalDateTime.now() + " ==== server uuid [" + LockService.getSystemuuid() + "]");
		List<InstanceStatus> status = InstanceService.getInstanceStatus();
		for (int i = 0; i < status.size(); i++) {
			InstanceStatus inst = status.get(i);
			ci.println(i + ") " + inst.getRemoteAddress() + " " + inst);
			long until = new Date().getTime() - inst.getLastUpdate().getTime();
			ci.print("\tFS:" + inst.getFirstSeen() + " LU:" + inst.getLastUpdate());
			if (until > 60 * 1000) {
				ci.print(" (!!!!)\n");
			} else {
				ci.print("\n");
			}
		}
	}

	public String __locks() {
		return getHelp(1);
	}

	public void __locks_list() {
		ci.println("======= " + LocalDateTime.now() + " ==== server uuid [" + LockService.getSystemuuid() + "]");
		for (LockInfo lockInfo : LockService.getAllLockInfo()) {
			ci.println(lockInfo.getUser() + "@" + lockInfo.getElementType() + "::" + lockInfo.getElementId() + "\t"
					+ lockInfo.getCreationDate() + "\t[" + lockInfo.getSystemUuid() + "]");
		}
	}

	public void __locks_clearAll() {
		LockService.clearAllLocks();
		ok();
	}

	public String __locks_clearSingle(Iterator<String> args) {
		if (args.hasNext()) {
			return Boolean.toString(LockService.clearLock(args.next()));
		} else {
			return missingArgument("elementId");
		}
	}

}
