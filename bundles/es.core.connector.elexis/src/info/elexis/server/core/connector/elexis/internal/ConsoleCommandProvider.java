package info.elexis.server.core.connector.elexis.internal;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.common.InstanceStatus;
import ch.elexis.core.console.AbstractConsoleCommandProvider;
import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.model.IConfig;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import info.elexis.server.core.connector.elexis.common.ElexisDBConnection;
import info.elexis.server.core.connector.elexis.instances.InstanceService;
import info.elexis.server.core.connector.elexis.services.LockService;
import info.elexis.server.core.connector.elexis.services.internal.CoreModelServiceHolder;

@Component(service = CommandProvider.class, immediate = true)
public class ConsoleCommandProvider extends AbstractConsoleCommandProvider {
	
	//	@Reference(cardinality = ReferenceCardinality.MANDATORY)
	//	private IStockCommissioningSystemService stockCommissioningSystemService;
	
	@Activate
	public void activate(){
		register(this.getClass());
	}
	
	public void _elc(CommandInterpreter ci){
		executeCommand("elc", ci);
	}
	
	public String __elc_status(){
		StringBuilder sb = new StringBuilder();
		sb.append("DB:\t\t" + ElexisDBConnection.getDatabaseInformationString() + "\n");
		sb.append("LS UUID:\t[" + LockService.getSystemuuid() + "]\n");
		sb.append("Locks:");
		for (LockInfo lockInfo : LockService.getAllLockInfo()) {
			sb.append("\t\t" + lockInfo.getUser() + "@" + lockInfo.getElementType() + "::"
				+ lockInfo.getElementId() + "\t" + lockInfo.getCreationDate() + "\t["
				+ lockInfo.getSystemUuid() + "]");
		}
		return sb.toString();
	}
	
	public void __elc_listInstances(){
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
	
	public String __elc_listInstances_clear(){
		InstanceService.clearInstanceStatus();
		return ok();
	}
	
	public void __elc_locks_list(){
		for (LockInfo lockInfo : LockService.getAllLockInfo()) {
			ci.println(lockInfo.getUser() + "@" + lockInfo.getElementType() + "::"
				+ lockInfo.getElementId() + "\t" + lockInfo.getCreationDate() + "\t["
				+ lockInfo.getSystemUuid() + "]");
		}
	}
	
	public void __elc_locks_clearAll(){
		LockService.clearAllLocks();
		ok();
	}
	
	public String __elc_locks_clearSingle(Iterator<String> args){
		if (args.hasNext()) {
			return Boolean.toString(LockService.clearLock(args.next()));
		} else {
			return missingArgument("elementId");
		}
	}
	
	public void __elc_config_list(Iterator<String> args){
		String nodePrefix = args.next();
		if (StringUtils.isEmpty(nodePrefix)) {
			nodePrefix = "";
		}
		
		IQuery<IConfig> qre = CoreModelServiceHolder.get().getQuery(IConfig.class);
		qre.and(ModelPackage.Literals.ICONFIG__KEY, COMPARATOR.LIKE, nodePrefix + "%");
		List<IConfig> nodes = qre.execute();
		for (IConfig config : nodes) {
			ci.println(config);
		}
	}
	
	//	public String __stock_list() {
	//		List<IStock> stocks = modelService.getQuery(IStock.class).execute();
	//		for (IStock stock : stocks) {
	//			ci.println(stock.getLabel());
	//			if (stock.isCommissioningSystem()) {
	//				ICommissioningSystemDriver instance = stockCommissioningSystemService
	//						.getDriverInstanceForStock(stock);
	//				ci.print("\t [  isCommissioningSystem  ] ");
	//				if (instance != null) {
	//					IStatus status = instance.getStatus();
	//					String statusString = StatusUtil.printStatus(status);
	//					ci.print(statusString);
	//				} else {
	//					ci.print("No driver instance found.\n");
	//				}
	//			}
	//		}
	//		return ok();
	//	}
	
	//	public String __stock_scs(Iterator<String> args) {
	//		String stockId = args.next();
	//		String action = args.next();
	//		if (stockId == null || action == null) {
	//			return missingArgument("stockId (start | stop)");
	//		}
	//
	//		Optional<IStock> findById = modelService.load(stockId, IStock.class);
	//		if (!findById.isPresent()) {
	//			return "Stock not found [" + stockId + "]";
	//		}
	//		IStatus status;
	//		if ("start".equalsIgnoreCase(action)) {
	//			status = stockCommissioningSystemService.initializeStockCommissioningSystem(findById.get());
	//		} else {
	//			status = stockCommissioningSystemService.shutdownStockCommissioningSytem(findById.get());
	//		}
	//		return StatusUtil.printStatus(status);
	//	}
	
	//	public String __stock_listForStock(Iterator<String> args) {
	//		if (args.hasNext()) {
	//			Optional<IStock> stock = modelService.load(args.next(), IStock.class);
	//			if (stock.isPresent()) {
	//				new StockService().findAllStockEntriesForStock(stock.get()).stream()
	//						.forEach(se -> ci.print(se.getLabel() + "\n"));
	//				return ok();
	//			} else {
	//				return "Invalid stock id";
	//			}
	//		} else {
	//			return missingArgument("stockId");
	//		}
	//	}
	
	//	public String __stock_seCsOut(Iterator<String> args) {
	//		if (args.hasNext()) {
	//			Optional<IStockEntry> se = modelService.load(args.next(), IStockEntry.class);
	//			if (se.isPresent()) {
	//				IStatus performArticleOutlay = stockCommissioningSystemService.performArticleOutlay(se.get(), 1,
	//						null);
	//				return StatusUtil.printStatus(performArticleOutlay);
	//			} else {
	//				return "Invalid stock entry id";
	//			}
	//		} else {
	//			return missingArgument("stockEntryId");
	//		}
	//	}
	
	//	public String __stock_stockSyncCs(Iterator<String> args) {
	//		if (args.hasNext()) {
	//			Optional<IStock> se = modelService.load(args.next(), IStock.class);
	//			if (se.isPresent()) {
	//				IStatus performArticleOutlay = stockCommissioningSystemService.synchronizeInventory(se.get(),
	//						Collections.emptyList(), null);
	//				return StatusUtil.printStatus(performArticleOutlay);
	//			} else {
	//				return "Invalid stock id";
	//			}
	//		} else {
	//			return missingArgument("stockId");
	//		}
	//	}
}
