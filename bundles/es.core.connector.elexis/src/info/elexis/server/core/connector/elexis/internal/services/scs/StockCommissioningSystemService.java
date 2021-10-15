package info.elexis.server.core.connector.elexis.internal.services.scs;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.stock.ICommissioningSystemDriver;
import ch.elexis.core.model.stock.ICommissioningSystemDriverFactory;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IStockCommissioningSystemService;
import ch.elexis.core.services.IStockService;
import ch.elexis.core.services.IStoreToStringService;
import ch.elexis.core.status.ObjectStatus;
import ch.elexis.core.status.StatusUtil;
import info.elexis.server.core.connector.elexis.internal.BundleConstants;
import info.elexis.server.core.connector.elexis.locking.ILockService;

@Component(property = "role=serverimpl")
public class StockCommissioningSystemService implements IStockCommissioningSystemService {
	
	private Map<String, ICommissioningSystemDriver> stockCommissioningSystemDriverInstances;
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;
	@Reference
	private IStockService stockService;
	@Reference
	private IStoreToStringService storeToStringService;
	@Reference
	private ICodeElementService codeElementService;
	@Reference
	private ILockService lockService;
	
	private Logger log;
	
	@Activate
	public void activate(){
		log = LoggerFactory.getLogger(getClass());
		stockCommissioningSystemDriverInstances = new HashMap<String, ICommissioningSystemDriver>();
	}
	
	@Override
	public List<UUID> listAllAvailableDrivers(){
		return StockCommissioningSystemDriverFactories.getAllDriverUuids();
	}
	
	@Override
	public String getInfoStringForDriver(UUID driverUuid, boolean extended){
		return StockCommissioningSystemDriverFactories.getInfoStringForDriver(driverUuid, extended);
	}
	
	@Override
	public IStatus initializeStockCommissioningSystem(IStock stock){
		UUID driver;
		try {
			String driverUuid = stock.getDriverUuid();
			if (driverUuid == null) {
				return new Status(Status.ERROR, BundleConstants.BUNDLE_ID,
					"Invalid SCSDriver UUID: " + stock.getDriverUuid());
			}
			driver = UUID.fromString(driverUuid);
		} catch (IllegalArgumentException iae) {
			return new Status(Status.ERROR, BundleConstants.BUNDLE_ID,
				"Invalid SCSDriver UUID: " + stock.getDriverUuid());
		}
		
		ICommissioningSystemDriverFactory icsdf =
			StockCommissioningSystemDriverFactories.getDriverFactory(driver);
		if (icsdf == null) {
			return new Status(Status.ERROR, BundleConstants.BUNDLE_ID,
				"SCSDriver factory not found: " + driver.toString());
		}
		
		ICommissioningSystemDriver icsd = icsdf.createDriverInstance();
		if (icsd == null) {
			return new Status(Status.ERROR, BundleConstants.BUNDLE_ID,
				"SCSDriver instance is null for UUID: " + stock.getDriverUuid());
		}
		
		String configuration = stock.getDriverConfig();
		IStatus status = icsd.initializeInstance(configuration, stock);
		if (status.isOK()) {
			stockCommissioningSystemDriverInstances.put(stock.getId(), icsd);
			return Status.OK_STATUS;
		}
		
		return status;
	}
	
	@Override
	public IStatus shutdownStockCommissioningSytem(IStock stock){
		ICommissioningSystemDriver icsd =
			stockCommissioningSystemDriverInstances.get(stock.getId());
		if (icsd == null) {
			return Status.OK_STATUS;
		}
		IStatus shutdownStatus = icsd.shutdownInstance();
		if (shutdownStatus.isOK()) {
			stockCommissioningSystemDriverInstances.remove(stock.getId());
		} else {
			log.warn("Problem shutting down commissioning system driver [{}]:"
				+ shutdownStatus.getMessage(), icsd.getClass().getName());
		}
		return shutdownStatus;
	}
	
	@Override
	public IStatus initializeInstancesUsingDriver(UUID driver){
		IQuery<IStock> sq = coreModelService.getQuery(IStock.class);
		sq.and(ModelPackage.Literals.ISTOCK__DRIVER_UUID, COMPARATOR.EQUALS, driver.toString());
		List<IStock> stocks = sq.execute();
		for (IStock stock : stocks) {
			IStatus status = initializeStockCommissioningSystem(stock);
			if (!status.isOK()) {
				return status;
			}
		}
		return Status.OK_STATUS;
	}
	
	@Override
	public IStatus shutdownInstancesUsingDriver(UUID driver){
		IQuery<IStock> sq = coreModelService.getQuery(IStock.class);
		sq.and(ModelPackage.Literals.ISTOCK__DRIVER_UUID, COMPARATOR.EQUALS, driver.toString());
		List<IStock> stocks = sq.execute();
		for (IStock stock : stocks) {
			IStatus status = shutdownStockCommissioningSytem(stock);
			if (!status.isOK()) {
				return status;
			}
		}
		return Status.OK_STATUS;
	}
	
	@Override
	public ICommissioningSystemDriver getDriverInstanceForStock(IStock stock){
		return stockCommissioningSystemDriverInstances.get(stock.getId());
	}
	
	@Override
	public IStatus performArticleOutlay(IStockEntry stockEntry, int quantity,
		Map<String, Object> data){
		IStock stock = stockEntry.getStock();
		ICommissioningSystemDriver ics = stockCommissioningSystemDriverInstances.get(stock.getId());
		if (ics == null) {
			IStatus icsStatus = initializeStockCommissioningSystem(stockEntry.getStock());
			if (!icsStatus.isOK()) {
				return icsStatus;
			} else {
				ics = stockCommissioningSystemDriverInstances.get(stock.getId());
				if (ics == null) {
					return new Status(Status.ERROR, BundleConstants.BUNDLE_ID,
						"Incorrect stock commissioning service initialization.");
				}
			}
		}
		
		IArticle article = stockEntry.getArticle();
		if (article == null) {
			return new Status(Status.ERROR, BundleConstants.BUNDLE_ID,
				"Could not resolve article in stockEntry");
		}
		return ics.performStockRemoval(article.getGtin(), quantity, data);
	}
	
	@Override
	public IStatus synchronizeInventory(IStock stock, List<String> gtinsToUpdate,
		Map<String, Object> data){
		ICommissioningSystemDriver ics = stockCommissioningSystemDriverInstances.get(stock.getId());
		if (ics == null) {
			IStatus icsStatus = initializeStockCommissioningSystem(stock);
			if (!icsStatus.isOK()) {
				return icsStatus;
			} else {
				ics = stockCommissioningSystemDriverInstances.get(stock.getId());
				if (ics == null) {
					return new Status(Status.ERROR, BundleConstants.BUNDLE_ID,
						"Incorrect stock commissioning service initialization, or is not machine stock.");
				}
			}
		}
		
		if (gtinsToUpdate == null) {
			gtinsToUpdate = Collections.emptyList();
		}
		
		IStatus retrieveInventory = ics.retrieveInventory(gtinsToUpdate, data);
		if (!retrieveInventory.isOK()) {
			return retrieveInventory;
		}
		
		ObjectStatus os = (ObjectStatus) retrieveInventory;
		List<IStockEntry> transientCommSysStockEntries = (List<IStockEntry>) os.getObject();
		
		log.trace("sychronizeInventory stock [{}] inventoryResultSize [{}] gtinsToUpdateSize [{}]",
			stock.getId(), transientCommSysStockEntries.size(), gtinsToUpdate.size());
		
		if (gtinsToUpdate.size() > 0) {
			return performDifferentialInventorySynchronization(stock, transientCommSysStockEntries,
				gtinsToUpdate);
		}
		return performFullInventorySynchronization(stock, transientCommSysStockEntries);
	}
	
	private IStatus performFullInventorySynchronization(IStock stock,
		List<? extends IStockEntry> inventoryResult){
		List<IStockEntry> currentStockEntries = stockService.findAllStockEntriesForStock(stock);
		Set<String> currentStockEntryIds =
			currentStockEntries.stream().map(cse -> cse.getId()).collect(Collectors.toSet());
		for (int i = 0; i < inventoryResult.size(); i++) {
			IStockEntry inventoryResultStockEntry = inventoryResult.get(i);
			String gtin = inventoryResultStockEntry.getArticle().getGtin();
			log.trace("{}/{} synchronize [{}] gtin [{}]", i, inventoryResult.size(),
				inventoryResultStockEntry.getId(), gtin);
			
			IStatus status = null;
			Optional<IStockEntry> seo = currentStockEntries.stream()
				.filter(s -> (gtin.equalsIgnoreCase(s.getArticle().getGtin()))).findFirst();
			if (seo.isPresent()) {
				status = updateStockEntry(seo.get(), inventoryResultStockEntry.getCurrentStock());
				currentStockEntryIds.remove(seo.get().getId());
			} else {
				status = createStockEntry(stock, inventoryResultStockEntry);
			}
			if (status != null && !status.isOK()) {
				StatusUtil.logStatus(log, status, true);
			}
		}
		
		// remove surplus stock entries
		for (String stockEntryId : currentStockEntryIds) {
			Optional<IStockEntry> seo = coreModelService.load(stockEntryId, IStockEntry.class);
			if (seo.isPresent()) {
				deleteStockEntry(seo.get());
			} else {
				log.error("StockEntry [{}] should be available!", stockEntryId);
			}
		}
		
		return Status.OK_STATUS;
	}
	
	private IStatus performDifferentialInventorySynchronization(IStock stock,
		List<? extends IStockEntry> scsInventoryResult, List<String> gtinsToUpdate){
		
		Map<String, IStockEntry> inventoryGtinMap = new HashMap<String, IStockEntry>();
		scsInventoryResult.stream()
			.forEach(ir -> inventoryGtinMap.put(ir.getArticle().getGtin(), ir));
		for (String gtin : gtinsToUpdate) {
			IStatus status = null;
			
			Optional<IStockEntry> seo = Optional.empty();
			Optional<IArticle> findArticleByGtin = codeElementService.findArticleByGtin(gtin);
			if (findArticleByGtin.isPresent()) {
				String storeToString =
					storeToStringService.storeToString(findArticleByGtin.get()).get();
				seo = Optional
					.ofNullable(stockService.findStockEntryForArticleInStock(stock, storeToString));
			}
			
			if (inventoryGtinMap.get(gtin) != null) {
				IStockEntry iStockEntry = inventoryGtinMap.get(gtin);
				if (seo.isPresent()) {
					// if in inventory result and stockEntry exists -> update
					status = updateStockEntry(seo.get(), iStockEntry.getCurrentStock());
				} else {
					// if in inventory result but stockEntry does not exist ->
					status = createStockEntry(stock, iStockEntry);
				}
			} else {
				// if not in inventory result but stockEntry exists -> remove
				if (seo.isPresent()) {
					status = deleteStockEntry(seo.get());
				}
			}
			if (status != null && !status.isOK()) {
				StatusUtil.logStatus(log, status, true);
			}
		}
		return Status.OK_STATUS;
	}
	
	private IStatus deleteStockEntry(IStockEntry se){
		if (isStockEntryBoundForReorder(se)) {
			updateStockEntry(se, 0);
		} else {
			log.debug("Removing StockEntry [{}] as MIN and MAX <= 0", se.getId());
			LockResponse lr = lockService.acquireLockBlocking(se, 5, null);
			if (lr.isOk()) {
				se.setCurrentStock(0);
				se.setDeleted(true);
				coreModelService.save(se);
				LockResponse lrs = lockService.releaseLock(lr.getLockInfo());
				if (!lrs.isOk()) {
					log.warn("Could not release lock for StockEntry [{}]", se.getId());
				}
			}
		}
		
		return Status.OK_STATUS;
	}
	
	private IStatus createStockEntry(IStock stock, IStockEntry tse){
		String gtin = tse.getArticle().getGtin();
		
		Optional<IArticle> articleByGTIN = codeElementService.findArticleByGtin(gtin);
		if (articleByGTIN.isPresent()) {
			IArticle adid = articleByGTIN.get();
			String storeToString = storeToStringService.storeToString(adid).orElse(null);
			IStockEntry se = stockService.storeArticleInStock(stock, storeToString);
			se.setCurrentStock(tse.getCurrentStock());
			coreModelService.save(se);
			
			log.debug("Adding StockEntry [{}] {}", se.getId(), tse.getCurrentStock());
			LockResponse lr = lockService.acquireLockBlocking(se, 5, null);
			if (lr.isOk()) {
				LockResponse lrs = lockService.releaseLock(lr.getLockInfo());
				if (!lrs.isOk()) {
					log.warn("Could not release lock for StockEntry [{}]", se.getId());
				}
			}
		} else {
			log.warn("Could not resolve article by GTIN [{}], will not consider in stock update.",
				gtin);
		}
		return Status.OK_STATUS;
	}
	
	private IStatus updateStockEntry(IStockEntry se, int currentStock){
		log.debug("Updating StockEntry [{}] {} -> {}", se.getId(), se.getCurrentStock(),
			currentStock);
		if (se.getCurrentStock() == currentStock) {
			return Status.OK_STATUS;
		}
		LockResponse lr = lockService.acquireLockBlocking(se, 10, null);
		if (lr.isOk()) {
			se.setCurrentStock(currentStock);
			coreModelService.save(se);
			LockResponse lrs = lockService.releaseLock(lr.getLockInfo());
			if (!lrs.isOk()) {
				log.warn("Could not release lock for StockEntry [{}]", se.getId());
			}
		} else {
			log.error("Could not acquire lock in updateStockEntry");
		}
		return Status.OK_STATUS;
	}
	
	/**
	 * Is this stock entry bound for reordering? This is the case if either a minimum or maximum
	 * stock amount is defined (i.e. > 0).
	 * 
	 * @param se
	 * @return
	 */
	private boolean isStockEntryBoundForReorder(IStockEntry se){
		return se.getMinimumStock() > 0 || se.getMaximumStock() > 0;
	}
	
}
