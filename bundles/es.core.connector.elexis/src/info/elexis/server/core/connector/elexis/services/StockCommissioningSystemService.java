package info.elexis.server.core.connector.elexis.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.article.IArticle;
import ch.elexis.core.stock.ICommissioningSystemDriver;
import ch.elexis.core.stock.ICommissioningSystemDriverFactory;
import ch.elexis.core.stock.IStock;
import ch.elexis.core.stock.IStockCommissioningSystemService;
import ch.elexis.core.stock.IStockEntry;
import info.elexis.server.core.connector.elexis.internal.BundleConstants;
import info.elexis.server.core.connector.elexis.internal.StockCommissioningSystemDriverFactories;

public class StockCommissioningSystemService implements IStockCommissioningSystemService {

	public static StockCommissioningSystemService INSTANCE = InstanceHolder.INSTANCE;

	private Logger log = LoggerFactory.getLogger(StockCommissioningSystemService.class);

	private Map<String, ICommissioningSystemDriver> stockCommissioningSystemDriverInstances;

	private static final class InstanceHolder {
		static final StockCommissioningSystemService INSTANCE = new StockCommissioningSystemService();
	}

	private StockCommissioningSystemService() {
		stockCommissioningSystemDriverInstances = new HashMap<String, ICommissioningSystemDriver>();
	}

	@Override
	public List<UUID> listAllAvailableDrivers() {
		return StockCommissioningSystemDriverFactories.getAllDriverUuids();
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
	public ICommissioningSystemDriver getDriverInstanceForStock(IStock stock) {
		return stockCommissioningSystemDriverInstances.get(stock.getId());
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

}
