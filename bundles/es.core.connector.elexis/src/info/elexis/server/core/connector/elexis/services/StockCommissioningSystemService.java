//package info.elexis.server.core.connector.elexis.services;
//
//import static ch.elexis.core.common.ElexisEventTopics.*;
//
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.Set;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//import org.eclipse.core.runtime.IStatus;
//import org.eclipse.core.runtime.Status;
//import org.osgi.service.component.annotations.Component;
//import org.osgi.service.event.Event;
//import org.osgi.service.event.EventConstants;
//import org.osgi.service.event.EventHandler;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import ch.elexis.core.lock.types.LockInfo;
//import ch.elexis.core.lock.types.LockResponse;
//import ch.elexis.core.model.IArticle;
//import ch.elexis.core.model.IStock;
//import ch.elexis.core.model.IStockEntry;
//
//import ch.elexis.core.model.stock.ICommissioningSystemDriver;
//import ch.elexis.core.model.stock.ICommissioningSystemDriverFactory;
//import ch.elexis.core.services.IStockCommissioningSystemService;
//import ch.elexis.core.status.ObjectStatus;
//import ch.elexis.core.status.StatusUtil;
//import info.elexis.server.core.connector.elexis.internal.BundleConstants;
//import info.elexis.server.core.connector.elexis.internal.StockCommissioningSystemDriverFactories;
//
//import info.elexis.server.core.connector.elexis.locking.LockServiceInstance;
//import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;
//
//public class StockCommissioningSystemService implements IStockCommissioningSystemService {
//
//	private Logger log = LoggerFactory.getLogger(StockCommissioningSystemService.class);
//
//	private static Map<String, ICommissioningSystemDriver> stockCommissioningSystemDriverInstances = new HashMap<String, ICommissioningSystemDriver>();
//
//	private StockService stockService = new StockService();
//
//	@Component(property = { EventConstants.EVENT_TOPIC + "=" + BASE + "*" })
//	public static class StockCommissioningSystemServiceEventHandler implements EventHandler {
//
//		private Logger log = LoggerFactory.getLogger(StockCommissioningSystemServiceEventHandler.class);
//
//		@Override
//		public void handleEvent(Event event) {
//			StockCommissioningSystemService scss = new StockCommissioningSystemService();
//
//			String topic = event.getTopic();
//			if (topic.endsWith(STOCK_COMMISSIONING_OUTLAY)) {
//				// perform an outlay
//				String stockEntryId = event.getProperty(STOCK_COMMISSIONING_PROPKEY_STOCKENTRY_ID).toString();
//				Optional<IStockEntry> se = StockEntryService.load(stockEntryId);
//				int quantity = 0;
//				try {
//					String property = (String) event.getProperty(STOCK_COMMISSIONING_PROPKEY_QUANTITY);
//					quantity = Integer.parseInt(property);
//				} catch (NumberFormatException nfe) {
//					log.error("Error parsing [{}]", nfe.getMessage());
//				}
//				if (se.isPresent()) {
//					IStatus performArticleOutlay = scss.performArticleOutlay(se.get(), quantity, null);
//					if (!performArticleOutlay.isOK()) {
//						StatusUtil.logStatus(log, performArticleOutlay, true);
//					} else {
//						log.debug("Outlayed [{}] pcs of StockEntry [{}]", quantity, se.get().getId());
//					}
//				} else {
//					log.error("Could not find StockEntry [{}]", stockEntryId);
//				}
//			} else if (topic.endsWith(STOCK_COMMISSIONING_SYNC_STOCK)) {
//				// Update stock for article list
//				String stockId = (String) event.getProperty(STOCK_COMMISSIONING_PROPKEY_STOCK_ID);
//				Optional<IStock> stock = StockService.load(stockId);
//				if (stock.isPresent()) {
//					List<String> articleIds = (List<String>) event
//							.getProperty(STOCK_COMMISSIONING_PROPKEY_LIST_ARTICLE_ID);
//					IStatus status = scss.synchronizeInventory(stock.get(), articleIds, null);
//					if (!status.isOK()) {
//						StatusUtil.logStatus(log, status, true);
//					}
//				} else {
//					log.warn("Could not resolve stock [{}], skipping update stock", stockId);
//				}
//			}
//		}
//	}
//
//	@Override
//	public List<UUID> listAllAvailableDrivers() {
//		return StockCommissioningSystemDriverFactories.getAllDriverUuids();
//	}
//
//	@Override
//	public ICommissioningSystemDriver getDriverInstanceForStock(IStock stock) {
//		return stockCommissioningSystemDriverInstances.get(stock.getId());
//	}
//
//	@Override
//	public String getInfoStringForDriver(UUID driverUuid, boolean extended) {
//		return StockCommissioningSystemDriverFactories.getInfoStringForDriver(driverUuid, extended);
//	}
//
//	@Override
//	public IStatus initializeStockCommissioningSystem(IStock stock) {
//		UUID driver;
//		try {
//			String driverUuid = stock.getDriverUuid();
//			if (driverUuid == null) {
//				return new Status(Status.ERROR, BundleConstants.BUNDLE_ID,
//						"Invalid SCSDriver UUID: " + stock.getDriverUuid());
//			}
//			driver = UUID.fromString(driverUuid);
//		} catch (IllegalArgumentException iae) {
//			return new Status(Status.ERROR, BundleConstants.BUNDLE_ID,
//					"Invalid SCSDriver UUID: " + stock.getDriverUuid());
//		}
//
//		ICommissioningSystemDriverFactory icsdf = StockCommissioningSystemDriverFactories.getDriverFactory(driver);
//		if (icsdf == null) {
//			return new Status(Status.ERROR, BundleConstants.BUNDLE_ID,
//					"SCSDriver factory not found: " + driver.toString());
//		}
//
//		ICommissioningSystemDriver icsd = icsdf.createDriverInstance();
//		if (icsd == null) {
//			return new Status(Status.ERROR, BundleConstants.BUNDLE_ID,
//					"SCSDriver instance is null for UUID: " + stock.getDriverUuid());
//		}
//
//		String configuration = stock.getDriverConfig();
//		IStatus status = icsd.initializeInstance(configuration, stock);
//		if (status.isOK()) {
//			stockCommissioningSystemDriverInstances.put(stock.getId(), icsd);
//			return Status.OK_STATUS;
//		}
//
//		return status;
//	}
//
//	@Override
//	public IStatus initializeInstancesUsingDriver(UUID identification) {
//		JPAQuery<IStock> sq = new JPAQuery<Stock>(Stock.class);
//		sq.add(Stock_.driverUuid, QUERY.EQUALS, identification.toString());
//		List<Stock> stocks = sq.execute();
//		for (Stock stock : stocks) {
//			IStatus status = initializeStockCommissioningSystem(stock);
//			if (!status.isOK()) {
//				return status;
//			}
//		}
//		return Status.OK_STATUS;
//	}
//
//	@Override
//	public IStatus shutdownStockCommissioningSytem(IStock stock) {
//		ICommissioningSystemDriver icsd = stockCommissioningSystemDriverInstances.get(stock.getId());
//		if (icsd == null) {
//			return Status.OK_STATUS;
//		}
//		IStatus shutdownStatus = icsd.shutdownInstance();
//		if (shutdownStatus.isOK()) {
//			stockCommissioningSystemDriverInstances.remove(stock.getId());
//		} else {
//			log.warn("Problem shutting down commissioning system driver [{}]:" + shutdownStatus.getMessage(),
//					icsd.getClass().getName());
//		}
//		return shutdownStatus;
//	}
//
//	@Override
//	public IStatus shutdownInstancesUsingDriver(UUID identification) {
//		JPAQuery<IStock> sq = new JPAQuery<Stock>(Stock.class);
//		sq.add(Stock_.driverUuid, QUERY.EQUALS, identification.toString());
//		List<Stock> stocks = sq.execute();
//		for (Stock stock : stocks) {
//			IStatus status = shutdownStockCommissioningSytem(stock);
//			if (!status.isOK()) {
//				return status;
//			}
//		}
//		return Status.OK_STATUS;
//	}
//
//	@Override
//	public IStatus performArticleOutlay(IStockEntry stockEntry, int quantity, Map<String, Object> data) {
//		IStock stock = stockEntry.getStock();
//		ICommissioningSystemDriver ics = stockCommissioningSystemDriverInstances.get(stock.getId());
//		if (ics == null) {
//			IStatus icsStatus = initializeStockCommissioningSystem(stockEntry.getStock());
//			if (!icsStatus.isOK()) {
//				return icsStatus;
//			} else {
//				ics = stockCommissioningSystemDriverInstances.get(stock.getId());
//				if (ics == null) {
//					return new Status(Status.ERROR, BundleConstants.BUNDLE_ID,
//							"Incorrect stock commissioning service initialization.");
//				}
//			}
//		}
//
//		IArticle article = stockEntry.getArticle();
//		if (article == null) {
//			return new Status(Status.ERROR, BundleConstants.BUNDLE_ID, "Could not resolve article in stockEntry");
//		}
//		return ics.performStockRemoval(article.getGTIN(), quantity, data);
//	}
//
//	@Override
//	public IStatus synchronizeInventory(IStock stock, List<String> gtinsToUpdate, Map<String, Object> data) {
//		ICommissioningSystemDriver ics = stockCommissioningSystemDriverInstances.get(stock.getId());
//		if (ics == null) {
//			IStatus icsStatus = initializeStockCommissioningSystem(stock);
//			if (!icsStatus.isOK()) {
//				return icsStatus;
//			} else {
//				ics = stockCommissioningSystemDriverInstances.get(stock.getId());
//				if (ics == null) {
//					return new Status(Status.ERROR, BundleConstants.BUNDLE_ID,
//							"Incorrect stock commissioning service initialization, or is not machine stock.");
//				}
//			}
//		}
//
//		if (gtinsToUpdate == null) {
//			gtinsToUpdate = Collections.emptyList();
//		}
//
//		IStatus retrieveInventory = ics.retrieveInventory(gtinsToUpdate, data);
//		if (!retrieveInventory.isOK()) {
//			return retrieveInventory;
//		}
//
//		ObjectStatus os = (ObjectStatus) retrieveInventory;
//		List<IStockEntry> transientCommSysStockEntries = (List<IStockEntry>) os.getObject();
//
//		log.trace("sychronizeInventory stock [{}] inventoryResultSize [{}] gtinsToUpdateSize [{}]", stock.getId(),
//				transientCommSysStockEntries.size(), gtinsToUpdate.size());
//
//		if (gtinsToUpdate.size() > 0) {
//			return performDifferentialInventorySynchronization(stock, transientCommSysStockEntries, gtinsToUpdate);
//		}
//		return performFullInventorySynchronization(stock, transientCommSysStockEntries);
//	}
//
//	private IStatus performDifferentialInventorySynchronization(IStock stock,
//			List<? extends IStockEntry> scsInventoryResult, List<String> gtinsToUpdate) {
//
//		Map<String, IStockEntry> inventoryGtinMap = new HashMap<String, IStockEntry>();
//		scsInventoryResult.stream().forEach(ir -> inventoryGtinMap.put(ir.getArticle().getGTIN(), ir));
//		for (String gtin : gtinsToUpdate) {
//			IStatus status = null;
//			Optional<IStockEntry> seo = new StockService().findStockEntryByGTINForStock(stock, gtin);
//			if (inventoryGtinMap.get(gtin) != null) {
//				IStockEntry iStockEntry = inventoryGtinMap.get(gtin);
//				if (seo.isPresent()) {
//					// if in inventory result and stockEntry exists -> update
//					status = updateStockEntry(seo.get(), iStockEntry.getCurrentStock());
//				} else {
//					// if in inventory result but stockEntry does not exist ->
//					status = createStockEntry(stock, iStockEntry);
//				}
//			} else {
//				// if not in inventory result but stockEntry exists -> remove
//				if (seo.isPresent()) {
//					status = deleteStockEntry(seo.get());
//				}
//			}
//			if (status != null && !status.isOK()) {
//				StatusUtil.logStatus(log, status, true);
//			}
//		}
//		return Status.OK_STATUS;
//	}
//
//	private IStatus performFullInventorySynchronization(IStock stock, List<? extends IStockEntry> inventoryResult) {
//		List<IStockEntry> currentStockEntries = stockService.findAllStockEntriesForStock(stock);
//		Set<String> currentStockEntryIds = currentStockEntries.stream().map(cse -> cse.getId())
//				.collect(Collectors.toSet());
//		for (IStockEntry inventoryResultStockEntry : inventoryResult) {
//			String gtin = inventoryResultStockEntry.getArticle().getGTIN();
//
//			IStatus status = null;
//			Optional<IStockEntry> seo = currentStockEntries.stream()
//					.filter(s -> (gtin.equalsIgnoreCase(s.getArticle().getGTIN()))).findFirst();
//			if (seo.isPresent()) {
//				status = updateStockEntry(seo.get(), inventoryResultStockEntry.getCurrentStock());
//				currentStockEntryIds.remove(seo.get().getId());
//			} else {
//				status = createStockEntry(stock, inventoryResultStockEntry);
//				currentStockEntryIds.remove(seo.get().getId());
//			}
//			if (status != null && !status.isOK()) {
//				StatusUtil.logStatus(log, status, true);
//			}
//		}
//
//		// remove surplus stock entries
//		for (String stockEntryId : currentStockEntryIds) {
//			Optional<IStockEntry> seo = StockEntryService.load(stockEntryId);
//			if (seo.isPresent()) {
//				deleteStockEntry(seo.get());
//			} else {
//				log.error("StockEntry [{}] should be available!", stockEntryId);
//			}
//		}
//
//		return Status.OK_STATUS;
//	}
//
//	private IStatus deleteStockEntry(IStockEntry se) {
//		if (StockEntryService.isStockEntryBoundForReorder(se)) {
//			updateStockEntry(se, 0);
//		} else {
//			log.debug("Removing StockEntry [{}] as MIN and MAX <= 0", ((IStockEntry) se).getId());
//			Optional<LockInfo> lr = LockServiceInstance.INSTANCE.acquireLockBlocking(se, 5);
//			if (lr.isPresent()) {
//				se.setCurrentStock(0);
//				se.setDeleted(true);
//				StockEntryService.save((IStockEntry) se);
//				LockResponse lrs = LockServiceInstance.INSTANCE.releaseLock(lr.get());
//				if (!lrs.isOk()) {
//					log.warn("Could not release lock for StockEntry [{}]", se.getId());
//				}
//			}
//		}
//
//		return Status.OK_STATUS;
//	}
//
//	private IStatus createStockEntry(IStock stock, IStockEntry tse) {
//		String gtin = tse.getArticle().getGTIN();
//		Optional<? extends IArticle> articleByGTIN = new ArticleService().findAnyByGTIN(gtin);
//		if (articleByGTIN.isPresent()) {
//			AbstractDBObjectIdDeleted adid = (AbstractDBObjectIdDeleted) articleByGTIN.get();
//			String storeToString = StoreToStringService.storeToString(adid);
//			IStockEntry se = (StockEntry) stockService.storeArticleInStock(stock, storeToString, tse.getCurrentStock());
//			log.debug("Adding StockEntry [{}] {}", se.getId(), tse.getCurrentStock());
//			Optional<LockInfo> lr = LockServiceInstance.INSTANCE.acquireLockBlocking(se, 5);
//			if (lr.isPresent()) {
//				LockResponse lrs = LockServiceInstance.INSTANCE.releaseLock(lr.get());
//				if (!lrs.isOk()) {
//					log.warn("Could not release lock for StockEntry [{}]", se.getId());
//				}
//			}
//		} else {
//			log.warn("Could not resolve article by GTIN [{}], will not consider in stock update.", gtin);
//		}
//		return Status.OK_STATUS;
//	}
//
//	private IStatus updateStockEntry(IStockEntry se, int currentStock) {
//		log.debug("Updating StockEntry [{}] {} -> {}", se.getId(), se.getCurrentStock(), currentStock);
//		if (se.getCurrentStock() == currentStock) {
//			return Status.OK_STATUS;
//		}
//		Optional<LockInfo> lr = LockServiceInstance.INSTANCE.acquireLockBlocking(se, 10);
//		if (lr.isPresent()) {
//			se.setCurrentStock(currentStock);
//			StockEntryService.save((IStockEntry) se);
//			LockResponse lrs = LockServiceInstance.INSTANCE.releaseLock(lr.get());
//			if (!lrs.isOk()) {
//				log.warn("Could not release lock for StockEntry [{}]", se.getId());
//			}
//		} else {
//			log.error("Could not acquire lock in updateStockEntry");
//		}
//		return Status.OK_STATUS;
//	}
//
//	public IStatus initializeAllInstances() {
//		List<UUID> allDriverUuids = StockCommissioningSystemDriverFactories.getAllDriverUuids();
//		for (UUID uuid : allDriverUuids) {
//			log.info("Initializing stock commissioning systems for driver id [{}]",
//					StockCommissioningSystemDriverFactories.getInfoStringForDriver(uuid, true));
//			IStatus status = initializeInstancesUsingDriver(uuid);
//			if (!status.isOK()) {
//				return status;
//			}
//		}
//		return Status.OK_STATUS;
//	}
//
//}
