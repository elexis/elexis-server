package info.elexis.server.core.connector.elexis.console;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Stock;
import info.elexis.server.core.connector.elexis.services.LockService;
import info.elexis.server.core.connector.elexis.services.StockService;

@Component(service = CommandProvider.class, immediate = true)
public class ConsoleCommandProvider implements CommandProvider {

	private Logger log = LoggerFactory.getLogger(ConsoleCommandProvider.class);

	public void _es_elc(CommandInterpreter ci) {
		final String argument = ci.nextArgument();
		if (argument == null) {
			ci.println(getHelp());
			return;
		}
		try {
			this.getClass().getMethod("__" + argument, CommandInterpreter.class).invoke(this, ci);
		} catch (Exception e) {
			log.error("Execution error on argument " + argument, e);
		}
	}

	public void __connectionStatus(CommandInterpreter ci) {
		IStatus dbi = ElexisDBConnection.getDatabaseInformation();
		StatusUtil.printStatus(System.out, dbi);
	}
	
	public void __listInstances(CommandInterpreter ci) {
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

	public void __listLocks(CommandInterpreter ci) {
		ci.println("======= " + LocalDateTime.now() + " ==== server uuid [" + LockService.getSystemuuid() + "]");
		for (LockInfo lockInfo : LockService.getAllLockInfo()) {
			ci.println(lockInfo.getUser() + "@" + lockInfo.getElementType() + "::" + lockInfo.getElementId() + "\t"
					+ lockInfo.getCreationDate() + "\t[" + lockInfo.getSystemUuid() + "]");
		}
	}

	public void __stockManagement(CommandInterpreter ci) {
		String nextArgument = ci.nextArgument();
		if (nextArgument == null) {
			List<Stock> findAll = StockService.INSTANCE.findAll(true);
			for (Stock stock : findAll) {
				ci.println(stock.getLabel());
			}
			ci.println("");
		}
	}

	@Override
	public String getHelp() {
		List<String> methods = getMethods();
		
		return "Usage: es_elc ("+methods.stream().reduce((u, t) -> u + " | " + t).get()+")";
	}

	private List<String> getMethods() {
		try {
			Method[] methods = this.getClass().getMethods();
			List<Method> asList = Arrays.asList(methods);
			return asList.stream().map(p -> p.getName()).filter(p -> p.startsWith("__")).map(p -> p.substring(2))
					.collect(Collectors.toList());
		} catch (Exception e) {}
		return Collections.emptyList();
	}
}
