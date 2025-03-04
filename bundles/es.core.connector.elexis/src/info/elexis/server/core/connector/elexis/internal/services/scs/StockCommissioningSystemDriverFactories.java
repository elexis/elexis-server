package info.elexis.server.core.connector.elexis.internal.services.scs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.IStatus;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.stock.ICommissioningSystemDriverFactory;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.IStockCommissioningSystemService;
import ch.elexis.core.services.holder.AccessControlServiceHolder;
import ch.elexis.core.status.StatusUtil;

@Component(service = {})
public class StockCommissioningSystemDriverFactories {

	@Reference(target = "(role=serverimpl)")
	private IStockCommissioningSystemService stockCommissioningSystemService;

	@Reference
	private IAccessControlService accessControlService;

	private Logger log = LoggerFactory.getLogger(StockCommissioningSystemDriverFactories.class);

	private static Map<UUID, ICommissioningSystemDriverFactory> driverFactories = new ConcurrentHashMap<UUID, ICommissioningSystemDriverFactory>();

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	public void bind(ICommissioningSystemDriverFactory driverFactory) {
		log.debug("Binding " + driverFactory.getClass().getName());
		driverFactories.put(driverFactory.getIdentification(), driverFactory);
	}

	@Activate
	public void activate() {
		log.trace("Initializing stock commissioning systems.");

		accessControlService.doPrivileged(() -> {
			List<UUID> allDriverUuids = getAllDriverUuids();
			for (UUID uuid : allDriverUuids) {
				log.info("Initializing stock commissioning systems for driver id [{}]",
						StockCommissioningSystemDriverFactories.getInfoStringForDriver(uuid, true));
				IStatus status = stockCommissioningSystemService.initializeInstancesUsingDriver(uuid);
				if (!status.isOK()) {
					StatusUtil.logStatus(log, status, true);
				}
			}
		});
	}

	public void unbind(ICommissioningSystemDriverFactory driverFactory) {
		log.debug("Unbinding " + driverFactory.getClass().getName());

		AccessControlServiceHolder.get().doPrivileged(() -> {
			log.info("Shutting down stock commissioning systems for driver id [{}]",
					driverFactory.getIdentification().toString());
			IStatus status = stockCommissioningSystemService
					.shutdownInstancesUsingDriver(driverFactory.getIdentification());
			if (!status.isOK()) {
				StatusUtil.logStatus(log, status, true);
			}
		});

		driverFactories.remove(driverFactory.getIdentification());
	}

	public static ICommissioningSystemDriverFactory getDriverFactory(UUID driver) {
		return driverFactories.get(driver);
	}

	public static List<UUID> getAllDriverUuids() {
		return new ArrayList<UUID>(driverFactories.keySet());
	}

	public static String getInfoStringForDriver(UUID driverUuid, boolean extended) {
		ICommissioningSystemDriverFactory icsdf = driverFactories.get(driverUuid);
		if (icsdf != null) {
			if (extended) {
				return icsdf.getName() + " (" + icsdf.getDescription() + ")";
			} else {
				return icsdf.getName();
			}
		}
		return "";
	}
}
