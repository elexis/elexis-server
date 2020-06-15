package info.elexis.server.core.connector.elexis.internal.services.scs;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.console.AbstractConsoleCommandProvider;
import ch.elexis.core.console.CmdAdvisor;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.stock.ICommissioningSystemDriver;
import ch.elexis.core.services.IStockCommissioningSystemService;
import ch.elexis.core.services.IStockService;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.status.StatusUtil;

@Component(service = CommandProvider.class, immediate = true)
public class StockConsoleCommandProvider extends AbstractConsoleCommandProvider {
	
	@Reference
	private IStockService stockService;
	
	@Reference(target = "(role=serverimpl)")
	private IStockCommissioningSystemService stockCommissioningSystemService;
	
	@Activate
	public void activate(){
		register(this.getClass());
	}
	
	@CmdAdvisor(description = "stock management")
	public void _stock(CommandInterpreter ci){
		executeCommand("stock", ci);
	}
	
	@CmdAdvisor(description = "list the defined stocks")
	public String __stock_list(){
		List<IStock> stocks = stockService.getAllStocks(true);
		for (IStock stock : stocks) {
			ci.println(stock.getLabel());
			if (stock.isCommissioningSystem()) {
				ICommissioningSystemDriver instance =
					stockCommissioningSystemService.getDriverInstanceForStock(stock);
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
	
	@CmdAdvisor(description = "list all stock entries for a given stockId")
	public void __stock_entries_list(String stockId){
		Optional<IStock> stock = CoreModelServiceHolder.get().load(stockId, IStock.class);
		if (!stock.isPresent()) {
			ci.println("Stock not found [" + stockId + "]");
		} else {
			List<IStockEntry> entries = stockService.findAllStockEntriesForStock(stock.get());
			for (IStockEntry iStockEntry : entries) {
				ci.println(iStockEntry);
			}
		}
	}
	
	@CmdAdvisor(description = "Start a stock commissioning system")
	public String __stock_scs_start(String stockId){
		Optional<IStock> findById = CoreModelServiceHolder.get().load(stockId, IStock.class);
		if (!findById.isPresent()) {
			return "Stock not found [" + stockId + "]";
		}
		IStatus status =
			stockCommissioningSystemService.initializeStockCommissioningSystem(findById.get());
		return StatusUtil.printStatus(status);
	}
	
	@CmdAdvisor(description = "Stop a stock commissioning system")
	public String __stock_scs_stop(String stockId){
		Optional<IStock> findById = CoreModelServiceHolder.get().load(stockId, IStock.class);
		if (!findById.isPresent()) {
			return "Stock not found [" + stockId + "]";
		}
		IStatus status =
			stockCommissioningSystemService.shutdownStockCommissioningSytem(findById.get());
		return StatusUtil.printStatus(status);
	}
	
	@CmdAdvisor(description = "Outlay a single element of a given stockEntryId")
	public String __stock_scs_outlay(String stockEntryId){
		Optional<IStockEntry> findById =
			CoreModelServiceHolder.get().load(stockEntryId, IStockEntry.class);
		if (!findById.isPresent()) {
			return "StockEntry not found [" + stockEntryId + "]";
		}
		
		IStatus performArticleOutlay =
			stockCommissioningSystemService.performArticleOutlay(findById.get(), 1, null);
		return StatusUtil.printStatus(performArticleOutlay);
	}
	
	@CmdAdvisor(description = "Synchronize the stock state of the commissioning system to Elexis")
	public String __stock_scs_sync(String stockId){
		Optional<IStock> findById = CoreModelServiceHolder.get().load(stockId, IStock.class);
		if (!findById.isPresent()) {
			return "Stock not found [" + stockId + "]";
		}
		
		IStatus performArticleOutlay = stockCommissioningSystemService
			.synchronizeInventory(findById.get(), Collections.emptyList(), null);
		return StatusUtil.printStatus(performArticleOutlay);
	}
	
}
