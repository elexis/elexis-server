package info.elexis.server.core.connector.elexis.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.model.article.IArticle;
import ch.elexis.core.status.ObjectStatus;
import ch.elexis.core.status.StatusUtil;
import ch.elexis.core.stock.ICommissioningSystemDriver;
import ch.elexis.core.stock.ICommissioningSystemDriverFactory;
import ch.elexis.core.stock.IStock;
import ch.elexis.core.stock.IStockCommissioningSystemService;
import ch.elexis.core.stock.IStockEntry;
import info.elexis.server.core.connector.elexis.internal.BundleConstants;
import info.elexis.server.core.connector.elexis.internal.StockCommissioningSystemDriverFactories;
import info.elexis.server.core.connector.elexis.jpa.StoreToStringService;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.StockEntry;
import info.elexis.server.core.connector.elexis.locking.LockServiceInstance;

public class StockCommissioningSystemService implements IStockCommissioningSystemService {

	public static StockCommissioningSystemService INSTANCE = InstanceHolder.INSTANCE;

	private Logger log = LoggerFactory.getLogger(StockCommissioningSystemService.class);

	private Map<String, ICommissioningSystemDriver> stockCommissioningSystemDriverInstances;

	private static final class InstanceHolder {
		static final StockCommissioningSystemService INSTANCE = new StockCommissioningSystemService();
	}

	@Component(property = { EventConstants.EVENT_TOPIC + "=" + ElexisEventTopics.TOPIC_BASE + "*" })
	public static class StockCommissioningSystemServiceEventHandler implements EventHandler {

		private Logger log = LoggerFactory.getLogger(StockCommissioningSystemServiceEventHandler.class);

		@Override
		public void handleEvent(Event event) {
			String topic = event.getTopic();
			if (topic.endsWith(ElexisEventTopics.TOPIC_STOCK_COMMISSIONING_OUTLAY)) {
				String stockEntryId = event
						.getProperty(ElexisEventTopics.TOPIC_STOCK_COMMISSIONING_PROPKEY_STOCKENTRY_ID).toString();
				Optional<StockEntry> se = StockEntryService.INSTANCE.findById(stockEntryId);
				int quantity = 0;
				try {
					String property = (String) event
							.getProperty(ElexisEventTopics.TOPIC_STOCK_COMMISSIONING_PROPKEY_QUANTITY);
					quantity = Integer.parseInt(property);
				} catch (NumberFormatException nfe) {
					log.error("Error parsing [{}]", nfe.getMessage());
				}
				if (se.isPresent()) {
					IStatus performArticleOutlay = StockCommissioningSystemService.INSTANCE
							.performArticleOutlay(se.get(), quantity, null);
					if (!performArticleOutlay.isOK()) {
						StatusUtil.logStatus(log, performArticleOutlay, true);
					}
				} else {
					log.error("Could not find StockEntry [{}]", stockEntryId);
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
		IStatus status = icsd.initializeInstance(configuration);
		if (status.isOK()) {
			stockCommissioningSystemDriverInstances.put(stock.getId(), icsd);
			return Status.OK_STATUS;
		}

		return status;
	}

	@Override
	public IStatus shutdownStockCommissioningSytem(IStock stock) {
		ICommissioningSystemDriver icsd = stockCommissioningSystemDriverInstances.get(stock.getId());
		if (icsd == null) {
			return new Status(Status.ERROR, BundleConstants.BUNDLE_ID, "Instance is not available.");
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
	public void shutdownInstances() {
		Collection<ICommissioningSystemDriver> driverInstances = stockCommissioningSystemDriverInstances.values();
		for (Iterator<ICommissioningSystemDriver> iterator = driverInstances.iterator(); iterator.hasNext();) {
			ICommissioningSystemDriver icsd = (ICommissioningSystemDriver) iterator.next();
			IStatus shutdownStatus = icsd.shutdownInstance();
			if (shutdownStatus.isOK()) {
				stockCommissioningSystemDriverInstances.remove(icsd);
			} else {
				log.warn("Problem shutting down commissioning system driver  [{}]:" + shutdownStatus.getMessage(),
						icsd.getClass().getName());
			}
		}
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
		}

		IArticle article = stockEntry.getArticle();
		if (article == null) {
			return new Status(Status.ERROR, BundleConstants.BUNDLE_ID, "Could not resolve article in stockEntry");
		}
		return ics.performStockRemoval(article.getGTIN(), quantity, data);
	}

	@Override
	public IStatus synchronizeInventory(IStock stock, String articleId, Object data) {
		ICommissioningSystemDriver ics = stockCommissioningSystemDriverInstances.get(stock.getId());
		if (ics == null) {
			IStatus icsStatus = initializeStockCommissioningSystem(stock);
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

		IStatus retrieveInventory = ics.retrieveInventory(articleId, data);
		if (retrieveInventory.isOK()) {
			ObjectStatus os = (ObjectStatus) retrieveInventory;
			List<IStockEntry> stockEntriesCs = (List<IStockEntry>) os.getObject();
			List<IStockEntry> workList = new ArrayList<IStockEntry>(stockEntriesCs);

			for (Iterator<IStockEntry> iterator = workList.iterator(); iterator.hasNext();) {
				IStockEntry tse = (IStockEntry) iterator.next();
				String gtin = tse.getArticle().getGTIN();

				Optional<ArtikelstammItem> findByGTIN = ArtikelstammItemService.findByGTIN(gtin);
				if (findByGTIN.isPresent()) {
					String storeToString = StoreToStringService.storeToString(findByGTIN.get());
					IStockEntry ise = StockService.INSTANCE.findStockEntryForArticleInStock(stock, storeToString);
					if (ise != null) {
						if (tse.getCurrentStock() != ise.getCurrentStock()) {
							StockEntry se = (StockEntry) ise;
							log.debug("Fixing stock for StockEntry [{}] {} -> {}", se.getId(), se.getCurrentStock(),
									tse.getCurrentStock());
							Optional<LockInfo> lr = LockServiceInstance.INSTANCE.acquireLockBlocking(se, 5);
							if (lr.isPresent()) {
								se.setCurrentStock(ise.getCurrentStock());
								StockEntryService.INSTANCE.write((StockEntry) se);
								LockServiceInstance.INSTANCE.releaseLock(lr.get());
								iterator.remove();
								continue;
							}
						}

					}
				}

				log.warn("Could not resolve article [{}], skipping StockEntry update.", gtin);
			}

			// TODO remainders in worklist?
		}

		return retrieveInventory;
	}

}
