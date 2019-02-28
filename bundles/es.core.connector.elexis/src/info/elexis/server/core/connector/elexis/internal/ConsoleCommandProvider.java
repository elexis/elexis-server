package info.elexis.server.core.connector.elexis.internal;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.eclipse.persistence.queries.ScrollableCursor;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.common.InstanceStatus;
import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.model.stock.ICommissioningSystemDriver;
import ch.elexis.core.status.StatusUtil;
import info.elexis.server.core.connector.elexis.common.ElexisDBConnection;
import info.elexis.server.core.connector.elexis.instances.InstanceService;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Config;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Stock;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.StockEntry;
import info.elexis.server.core.connector.elexis.services.ConfigService;
import info.elexis.server.core.connector.elexis.services.JPAQuery;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;
import info.elexis.server.core.connector.elexis.services.LockService;
import info.elexis.server.core.connector.elexis.services.StockCommissioningSystemService;
import info.elexis.server.core.connector.elexis.services.StockEntryService;
import info.elexis.server.core.connector.elexis.services.StockService;
import info.elexis.server.core.console.AbstractConsoleCommandProvider;

@Component(service = CommandProvider.class, immediate = true)
public class ConsoleCommandProvider extends AbstractConsoleCommandProvider {

	public void _es_elc(CommandInterpreter ci) {
		executeCommand(ci);
	}

	public String __status() {
		StringBuilder sb = new StringBuilder();
		sb.append("DB:\t\t" + ElexisDBConnection.getDatabaseInformationString() + "\n");
		sb.append("LS UUID:\t[" + LockService.getSystemuuid() + "]\n");
		sb.append("Locks:");
		for (LockInfo lockInfo : LockService.getAllLockInfo()) {
			sb.append("\t\t" + lockInfo.getUser() + "@" + lockInfo.getElementType() + "::" + lockInfo.getElementId()
					+ "\t" + lockInfo.getCreationDate() + "\t[" + lockInfo.getSystemUuid() + "]");
		}
		return sb.toString();
	}

	public void __listInstances() {
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

	public String __listInstances_clear() {
		InstanceService.clearInstanceStatus();
		return ok();
	}

	public String __locks() {
		return getHelp(1);
	}

	public void __locks_list() {
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
	
	public String __config() {
		return getHelp(1);
	}
	
	public void __config_list(Iterator<String> args) {
		String keyPrefix = args.next();
		
		List<Config> nodes = ConfigService.INSTANCE.getNodes(keyPrefix);
		for (Config config : nodes) {
			ci.println(config);
		}
	}

	public String __entities() {
		return getHelp(1);
	}

	@SuppressWarnings("unchecked")
	public String __entities_list(Iterator<String> args) {
		String entity = args.next();
		if(StringUtils.isEmpty(entity)) {
			return missingArgument("Entity");
		}
		
		String includeDeletedString = args.next();
		boolean includeDeleted = true;
		if (!StringUtils.isAlpha(entity)) {
			return getHelp(2);
		}
		if (StringUtils.isNotEmpty(includeDeletedString)) {
			includeDeleted = Boolean.valueOf(includeDeletedString);
		}

		entity = StringUtils.capitalize(entity);

		Class<?> clazz;
		try {
			String PREFIX = "info.elexis.server.core.connector.elexis.jpa.model.annotated";
			clazz = ConsoleCommandProvider.class.getClassLoader().loadClass(PREFIX + "." + entity);
			if (AbstractDBObjectIdDeleted.class.isAssignableFrom(clazz)) {
				// replace with JPAQuery cursor, honor CTRL+C??

				@SuppressWarnings("rawtypes")
				JPAQuery<? extends AbstractDBObjectIdDeleted> query = new JPAQuery(clazz);
				if (!includeDeleted) {
					query.add(AbstractDBObjectIdDeleted_.deleted, QUERY.EQUALS, false);
				}
				long count = query.count();
				ScrollableCursor cursor = query.executeAsStream();
				while (cursor.hasNext()) {
					ci.println(cursor.next());
					cursor.clear();
				}
				cursor.close();
				return "-- " + count + " entries found, including deleted = " + Boolean.toString(includeDeleted);
			} else {
				return "Not instance of AbstractDBObjectIdDeleted";
			}
		} catch (ClassNotFoundException e) {
			return e.getMessage();
		}
	}

	public String __stock() {
		return getHelp(1);
	}

	public String __stock_list() {
		List<Stock> stocks = StockService.findAll(true);
		for (Stock stock : stocks) {
			ci.println(stock.getLabel());
			if (stock.isCommissioningSystem()) {
				ICommissioningSystemDriver instance = new StockCommissioningSystemService()
						.getDriverInstanceForStock(stock);
				ci.print("\t [  isCommissioningSystem  ] ");
				if (instance != null) {
					IStatus status = instance.getStatus();
					String statusString = StatusUtil.printStatus(status);
					ci.print(statusString);
				} else {
					ci.print("No driver instance found.\n");
				}
			}
		}
		return ok();
	}

	public String __stock_scs(Iterator<String> args) {
		String stockId = args.next();
		String action = args.next();
		if (stockId == null || action == null) {
			return missingArgument("stockId (start | stop)");
		}

		Optional<Stock> findById = StockService.load(stockId);
		if (!findById.isPresent()) {
			return "Stock not found [" + stockId + "]";
		}
		IStatus status;
		if ("start".equalsIgnoreCase(action)) {
			status = new StockCommissioningSystemService().initializeStockCommissioningSystem(findById.get());
		} else {
			status = new StockCommissioningSystemService().shutdownStockCommissioningSytem(findById.get());
		}
		return StatusUtil.printStatus(status);
	}

	public String __stock_listForStock(Iterator<String> args) {
		if (args.hasNext()) {
			Optional<Stock> stock = StockService.load(args.next());
			if (stock.isPresent()) {
				new StockService().findAllStockEntriesForStock(stock.get()).stream()
						.forEach(se -> ci.print(se.getLabel() + "\n"));
				return ok();
			} else {
				return "Invalid stock id";
			}
		} else {
			return missingArgument("stockId");
		}
	}

	public String __stock_seCsOut(Iterator<String> args) {
		if (args.hasNext()) {
			Optional<StockEntry> se = StockEntryService.load(args.next());
			if (se.isPresent()) {
				IStatus performArticleOutlay = new StockCommissioningSystemService().performArticleOutlay(se.get(), 1,
						null);
				return StatusUtil.printStatus(performArticleOutlay);
			} else {
				return "Invalid stock entry id";
			}
		} else {
			return missingArgument("stockEntryId");
		}
	}

	public String __stock_stockSyncCs(Iterator<String> args) {
		if (args.hasNext()) {
			Optional<Stock> se = StockService.load(args.next());
			if (se.isPresent()) {
				IStatus performArticleOutlay = new StockCommissioningSystemService().synchronizeInventory(se.get(),
						Collections.emptyList(), null);
				return StatusUtil.printStatus(performArticleOutlay);
			} else {
				return "Invalid stock id";
			}
		} else {
			return missingArgument("stockId");
		}
	}
}
