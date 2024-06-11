package info.elexis.server.core.connector.elexis.internal.services.scs;

import static ch.elexis.core.common.ElexisEventTopics.BASE_STOCK_COMMISSIONING;
import static ch.elexis.core.common.ElexisEventTopics.STOCK_COMMISSIONING_OUTLAY;
import static ch.elexis.core.common.ElexisEventTopics.STOCK_COMMISSIONING_PROPKEY_LIST_ARTICLE_ID;
import static ch.elexis.core.common.ElexisEventTopics.STOCK_COMMISSIONING_PROPKEY_QUANTITY;
import static ch.elexis.core.common.ElexisEventTopics.STOCK_COMMISSIONING_PROPKEY_STOCKENTRY_ID;
import static ch.elexis.core.common.ElexisEventTopics.STOCK_COMMISSIONING_PROPKEY_STOCK_ID;
import static ch.elexis.core.common.ElexisEventTopics.STOCK_COMMISSIONING_SYNC_STOCK;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IStockCommissioningSystemService;
import ch.elexis.core.status.StatusUtil;

@Component(property = {
	EventConstants.EVENT_TOPIC + "=" + BASE_STOCK_COMMISSIONING + "*", 
	EventConstants.EVENT_TOPIC + "=" +"remote/"+  BASE_STOCK_COMMISSIONING + "*"
})
public class StockCommissioningSystemServiceEventHandler implements EventHandler {
	
	@Reference(target = "(role=serverimpl)")
	private IStockCommissioningSystemService scss;
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;
	
	@Reference
	private IAccessControlService accessControlService;

	private Logger log;
	
	@Activate
	public void activate(){
		log = LoggerFactory.getLogger(getClass());
	}
	
	/**
	 * handle incoming requests to outlay articles
	 */
	@Override
	public void handleEvent(Event event){
		accessControlService.doPrivileged(() -> doHandleEvent(event));
	}

	private void doHandleEvent(Event event) {
		String topic = event.getTopic();
		if (topic.endsWith(STOCK_COMMISSIONING_OUTLAY)) {
			// perform an outlay
			String stockEntryId =
				event.getProperty(STOCK_COMMISSIONING_PROPKEY_STOCKENTRY_ID).toString();
			Optional<IStockEntry> se = coreModelService.load(stockEntryId, IStockEntry.class);
			int quantity = 0;
			try {
				String property = (String) event.getProperty(STOCK_COMMISSIONING_PROPKEY_QUANTITY);
				quantity = Integer.parseInt(property);
			} catch (NumberFormatException nfe) {
				log.error("Error parsing [{}]", nfe.getMessage());
			}
			if (se.isPresent()) {
				IStatus performArticleOutlay = scss.performArticleOutlay(se.get(), quantity, null);
				if (!performArticleOutlay.isOK()) {
					StatusUtil.logStatus(log, performArticleOutlay, true);
				} else {
					log.debug("Outlayed [{}] pcs of StockEntry [{}]", quantity, se.get().getId());
				}
			} else {
				log.error("Could not find StockEntry [{}]", stockEntryId);
			}
		} else if (topic.endsWith(STOCK_COMMISSIONING_SYNC_STOCK)) {
			// Update stock for article list
			String stockId = (String) event.getProperty(STOCK_COMMISSIONING_PROPKEY_STOCK_ID);
			Optional<IStock> stock = coreModelService.load(stockId, IStock.class);
			if (stock.isPresent()) {
				List<String> articleIds =
					(List<String>) event.getProperty(STOCK_COMMISSIONING_PROPKEY_LIST_ARTICLE_ID);
				IStatus status = scss.synchronizeInventory(stock.get(), articleIds, null);
				if (!status.isOK()) {
					StatusUtil.logStatus(log, status, true);
				}
			} else {
				log.warn("Could not resolve stock [{}], skipping update stock", stockId);
			}
		}
	}
}
