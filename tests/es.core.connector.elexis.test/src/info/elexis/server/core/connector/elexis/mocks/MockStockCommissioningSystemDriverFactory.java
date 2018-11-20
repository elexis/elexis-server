//package info.elexis.server.core.connector.elexis.mocks;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import org.eclipse.core.runtime.IStatus;
//import org.eclipse.core.runtime.Status;
//import org.osgi.service.component.annotations.Component;
//
//import ch.elexis.core.model.IStock;
//import ch.elexis.core.model.IStockEntry;
//import ch.elexis.core.model.stock.ICommissioningSystemDriver;
//import ch.elexis.core.model.stock.ICommissioningSystemDriverFactory;
//import info.elexis.server.core.connector.elexis.AllTestsSuite;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Stock;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.StockEntry;
//import info.elexis.server.core.connector.elexis.services.StockService;
//
//@Component
//public class MockStockCommissioningSystemDriverFactory implements ICommissioningSystemDriverFactory {
//
//	public static UUID uuid = UUID.fromString("e02d0db0-b480-4f23-82c7-5e29e83d5f6b");
//	
//	@Override
//	public UUID getIdentification() {
//		return uuid;
//	}
//
//	@Override
//	public String getName() {
//		return "Mock Stock Commissioning System";
//	}
//
//	@Override
//	public String getDescription() {
//		return "Mock Stock Commissioning System description";
//	}
//
//	@Override
//	public ICommissioningSystemDriver createDriverInstance() {
//		return new ICommissioningSystemDriver() {
//
//			@Override
//			public IStatus shutdownInstance() {
//				return Status.OK_STATUS;
//			}
//
//			@Override
//			public IStatus retrieveInventory(List<String> articleIds, Object data) {
//				return Status.OK_STATUS;
//			}
//
//			@Override
//			public IStatus performStockRemoval(String articleId, int quantity, Object data) {
//
//				Optional<Stock> load = StockService.load(AllTestsSuite.RWA_ID);
//				Optional<StockEntry> findStockEntryByGTINForStock = new StockService()
//						.findStockEntryByGTINForStock(load.get(), articleId);
//				IStockEntry stockEntry = findStockEntryByGTINForStock.get();
//				int currentStock = stockEntry.getCurrentStock();
//				stockEntry.setCurrentStock(currentStock - quantity);
//				StockService.save((AbstractDBObjectIdDeleted) stockEntry);
//
//				return Status.OK_STATUS;
//			}
//
//			@Override
//			public IStatus initializeInstance(String configuration, IStock stock) {
//				return Status.OK_STATUS;
//			}
//
//			@Override
//			public IStatus getStatus() {
//				return Status.OK_STATUS;
//			}
//		};
//	}
//
//}
