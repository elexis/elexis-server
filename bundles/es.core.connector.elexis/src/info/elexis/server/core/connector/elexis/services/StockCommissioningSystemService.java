package info.elexis.server.core.connector.elexis.services;

import static ch.elexis.core.common.ElexisEventTopics.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.article.IArticle;
import ch.elexis.core.model.stock.ICommissioningSystemDriver;
import ch.elexis.core.model.stock.ICommissioningSystemDriverFactory;
import ch.elexis.core.services.IStockCommissioningSystemService;
import ch.elexis.core.status.ObjectStatus;
import ch.elexis.core.status.StatusUtil;
import info.elexis.server.core.connector.elexis.internal.BundleConstants;
import info.elexis.server.core.connector.elexis.internal.StockCommissioningSystemDriverFactories;
import info.elexis.server.core.connector.elexis.jpa.StoreToStringService;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Stock;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.StockEntry;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Stock_;
import info.elexis.server.core.connector.elexis.locking.LockServiceInstance;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;

public class StockCommissioningSystemService implements IStockCommissioningSystemService {

	public static StockCommissioningSystemService INSTANCE = InstanceHolder.INSTANCE;

	private Logger log = LoggerFactory.getLogger(StockCommissioningSystemService.class);

	private Map<String, ICommissioningSystemDriver> stockCommissioningSystemDriverInstances;

	private static final class InstanceHolder {
		static final StockCommissioningSystemService INSTANCE = new StockCommissioningSystemService();
	}

	@Component(property = { EventConstants.EVENT_TOPIC + "=" + TOPIC_BASE + "*" })
	public static class StockCommissioningSystemServiceEventHandler implements EventHandler {

		private Logger log = LoggerFactory.getLogger(StockCommissioningSystemServiceEventHandler.class);

		@Override
		public void handleEvent(Event event) {
			String topic = event.getTopic();
			if (topic.endsWith(TOPIC_STOCK_COMMISSIONING_OUTLAY)) {
				// perform an outlay
				String stockEntryId = event.getProperty(TOPIC_STOCK_COMMISSIONING_PROPKEY_STOCKENTRY_ID).toString();
				Optional<StockEntry> se = StockEntryService.INSTANCE.findById(stockEntryId);
				int quantity = 0;
				try {
					String property = (String) event.getProperty(TOPIC_STOCK_COMMISSIONING_PROPKEY_QUANTITY);
					quantity = Integer.parseInt(property);
				} catch (NumberFormatException nfe) {
					log.error("Error parsing [{}]", nfe.getMessage());
				}
				if (se.isPresent()) {
					IStatus performArticleOutlay = StockCommissioningSystemService.INSTANCE
							.performArticleOutlay(se.get(), quantity, null);
					if (!performArticleOutlay.isOK()) {
						StatusUtil.logStatus(log, performArticleOutlay, true);
					} else {
						log.debug("Outlayed [{}] pcs of StockEntry [{}]", quantity, se.get().getId());
					}
				} else {
					log.error("Could not find StockEntry [{}]", stockEntryId);
				}
			} else if (topic.endsWith(TOPIC_STOCK_COMMISSIONING_SYNC_STOCK)) {
				// Update stock for article list
				String stockId = (String) event.getProperty(TOPIC_STOCK_COMMISSIONING_PROPKEY_STOCK_ID);
				Optional<Stock> stock = StockService.INSTANCE.findById(stockId);
				if (stock.isPresent()) {
					List<String> articleIds = (List<String>) event
							.getProperty(TOPIC_STOCK_COMMISSIONING_PROPKEY_LIST_ARTICLE_ID);
					IStatus status = StockCommissioningSystemService.INSTANCE.synchronizeInventory(stock.get(),
							articleIds, null);
					if (!status.isOK()) {
						StatusUtil.logStatus(log, status, true);
					}
				} else {
					log.warn("Could not resolve stock [{}], skipping update stock", stockId);
				}
			}
		}
	}

	private StockCommissioningSystemService() {
		stockCommissioningSystemDriverInstances = new HashMap<String, ICommissioningSystemDriver>();
	}

	@Override
	public List<UUID> listAllAvailableDrivers() {
		return StockCommissioningSystemDriverFactories.getAllDriverUuids();
	}

	@Override
	public ICommissioningSystemDriver getDriverInstanceForStock(IStock stock) {
		return stockCommissioningSystemDriverInstances.get(stock.getId());
	}

	@Override
	public String getInfoStringForDriver(UUID driverUuid, boolean extended) {
		return StockCommissioningSystemDriverFactories.getInfoStringForDriver(driverUuid, extended);
	}

	@Override
	public IStatus initializeStockCommissioningSystem(IStock stock) {
		UUID driver;
		try {
			driver = UUID.fromString(stock.getDriverUuid());
		} catch (IllegalArgumentException iae) {
			return new Status(Status.ERROR, BundleConstants.BUNDLE_ID,
					"Invalid SCSDriver UUID: " + stock.getDriverUuid());
		}

		ICommissioningSystemDriverFactory icsdf = StockCommissioningSystemDriverFactories.getDriverFactory(driver);
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
	public IStatus initializeInstancesUsingDriver(UUID identification) {
		JPAQuery<Stock> sq = new JPAQuery<Stock>(Stock.class);
		sq.add(Stock_.driverUuid, QUERY.EQUALS, identification.toString());
		List<Stock> stocks = sq.execute();
		for (Stock stock : stocks) {
			IStatus status = initializeStockCommissioningSystem(stock);
			if (!status.isOK()) {
				return status;
			}
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus shutdownStockCommissioningSytem(IStock stock) {
		ICommissioningSystemDriver icsd = stockCommissioningSystemDriverInstances.get(stock.getId());
		if (icsd == null) {
			return Status.OK_STATUS;
		}
		IStatus shutdownStatus = icsd.shutdownInstance();
		if (shutdownStatus.isOK()) {
			stockCommissioningSystemDriverInstances.remove(stock.getId());
		} else {
			log.warn("Problem shutting down commissioning system driver [{}]:" + shutdownStatus.getMessage(),
					icsd.getClass().getName());
		}
		return shutdownStatus;
	}

	@Override
	public IStatus shutdownInstancesUsingDriver(UUID identification) {
		JPAQuery<Stock> sq = new JPAQuery<Stock>(Stock.class);
		sq.add(Stock_.driverUuid, QUERY.EQUALS, identification.toString());
		List<Stock> stocks = sq.execute();
		for (Stock stock : stocks) {
			IStatus status = shutdownStockCommissioningSytem(stock);
			if (!status.isOK()) {
				return status;
			}
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus performArticleOutlay(IStockEntry stockEntry, int quantity, Object data) {
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
		} else {
			log.warn("Received performArticleOutlay but driver is [null]");
		}

		IArticle article = stockEntry.getArticle();
		if (article == null) {
			return new Status(Status.ERROR, BundleConstants.BUNDLE_ID, "Could not resolve article in stockEntry");
		}
		return ics.performStockRemoval(article.getGTIN(), quantity, data);
	}

	@Override
	public IStatus synchronizeInventory(IStock stock, List<String> articleIds, Object data) {
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

		IStatus retrieveInventory = ics.retrieveInventory(articleIds, data);
		if (!retrieveInventory.isOK()) {
			return retrieveInventory;
		}

		ObjectStatus os = (ObjectStatus) retrieveInventory;
		List<IStockEntry> transientCommSysStockEntries = (List<IStockEntry>) os.getObject();
		return synchronizeInventory(stock, transientCommSysStockEntries, articleIds);
	}

	/**
	 * 
	 * @param stock
	 *            the stock to sync upon
	 * @param inventoryResult
	 *            the incoming inventory result to sync the provided stock upon
	 * @param articleId
	 *            s
	 * @param fullSync
	 *            if <code>true</code> remove surplus stock entries
	 * @return
	 */
	IStatus synchronizeInventory(IStock stock, List<? extends IStockEntry> inventoryResult, List<String> articleIds) {
		log.trace("sychronizeInventory stock [{}] inventoryResultSize [{}] fullSync [{}]", stock.getId(),
				inventoryResult.size());

		List<StockEntry> currentStockEntries = StockService.INSTANCE.findAllStockEntriesForStock(stock);
		for (Iterator<? extends IStockEntry> iterator = inventoryResult.iterator(); iterator.hasNext();) {
			IStockEntry tse = (IStockEntry) iterator.next();
			String gtin = tse.getArticle().getGTIN();

			Optional<StockEntry> seo = currentStockEntries.stream()
					.filter(s -> (gtin.equalsIgnoreCase(s.getArticle().getGTIN()))).findFirst();
			if (seo.isPresent()) {
				// modify existing stock entry
				StockEntry se = seo.get();
				StockEntryService.INSTANCE.refresh(se);
				log.debug("Updating StockEntry [{}] {} -> {}", se.getId(), se.getCurrentStock(), tse.getCurrentStock());
				Optional<LockInfo> lr = LockServiceInstance.INSTANCE.acquireLockBlocking(se, 5);
				if (lr.isPresent()) {
					se.setCurrentStock(tse.getCurrentStock());
					StockEntryService.INSTANCE.write((StockEntry) se);
					LockResponse lrs = LockServiceInstance.INSTANCE.releaseLock(lr.get());
					if (!lrs.isOk()) {
						log.warn("Could not release lock for StockEntry [{}]", se.getId());
					}
					currentStockEntries.remove(se);
					iterator.remove();
				} else {
					log.error("Could not acquire lock");
				}
			} else {
				// create stock entry
				Optional<? extends IArticle> articleByGTIN = new ArticleService().findAnyByGTIN(gtin);
				if (articleByGTIN.isPresent()) {
					AbstractDBObjectIdDeleted adid = (AbstractDBObjectIdDeleted) articleByGTIN.get();
					String storeToString = StoreToStringService.storeToString(adid);
					StockEntry se = (StockEntry) StockService.INSTANCE.storeArticleInStock(stock, storeToString,
							tse.getCurrentStock());
					log.info("Adding StockEntry [{}] {}", se.getId(), tse.getCurrentStock());
					Optional<LockInfo> lr = LockServiceInstance.INSTANCE.acquireLockBlocking(se, 5);
					if (lr.isPresent()) {
						LockResponse lrs = LockServiceInstance.INSTANCE.releaseLock(lr.get());
						if (!lrs.isOk()) {
							log.warn("Could not release lock for StockEntry [{}]", se.getId());
						}
					}
					currentStockEntries.remove(se);
					iterator.remove();
				} else {
					log.warn("Could not resolve article by GTIN [{}], will not consider in stock update.", gtin);
				}
			}
		}

		boolean selectiveSync = false;
		Set<String> articleIdsToSync = new HashSet<String>(articleIds);
		if (articleIds != null && articleIds.size() > 0) {
			articleIdsToSync = new HashSet<String>(articleIds);
			selectiveSync = true;
		}

		// remove surplus stock entries
		for (Iterator<? extends IStockEntry> iterator = currentStockEntries.iterator(); iterator.hasNext();) {
			IStockEntry tse = (IStockEntry) iterator.next();
			if (selectiveSync) {
				IArticle article = tse.getArticle();
				if (article.getGTIN() == null || !articleIdsToSync.contains(article.getGTIN())) {
					continue;
				}
			}

			StockEntry se = (StockEntry) tse;
			log.debug("Removing StockEntry [{}]", ((StockEntry) tse).getId());
			Optional<LockInfo> lr = LockServiceInstance.INSTANCE.acquireLockBlocking(se, 5);
			if (lr.isPresent()) {
				se.setCurrentStock(0);
				se.setDeleted(true);
				StockEntryService.INSTANCE.write((StockEntry) se);
				LockResponse lrs = LockServiceInstance.INSTANCE.releaseLock(lr.get());
				if (!lrs.isOk()) {
					log.warn("Could not release lock for StockEntry [{}]", se.getId());
				}
			}
		}

		return Status.OK_STATUS;
	}

	public IStatus initializeAllInstances() {
		List<UUID> allDriverUuids = StockCommissioningSystemDriverFactories.getAllDriverUuids();
		for (UUID uuid : allDriverUuids) {
			log.info("Initializing stock commissioning systems for driver id [{}]",
					StockCommissioningSystemDriverFactories.getInfoStringForDriver(uuid, true));
			IStatus status = initializeInstancesUsingDriver(uuid);
			if (!status.isOK()) {
				return status;
			}
		}
		return Status.OK_STATUS;
	}

}
