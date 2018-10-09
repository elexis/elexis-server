//package info.elexis.server.core.connector.elexis.services;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//import javax.persistence.EntityManager;
//import javax.persistence.Query;
//
//import org.eclipse.core.runtime.IStatus;
//import org.eclipse.core.runtime.Status;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import ch.elexis.core.constants.Preferences;
//import ch.elexis.core.constants.StringConstants;
//import ch.elexis.core.lock.types.LockInfo;
//import ch.elexis.core.model.IStock;
//import ch.elexis.core.model.IStockEntry;
//import ch.elexis.core.model.article.IArticle;
//import ch.elexis.core.services.IStockService;
//import info.elexis.server.core.connector.elexis.internal.BundleConstants;
//import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
//import info.elexis.server.core.connector.elexis.jpa.QueryConstants;
//import info.elexis.server.core.connector.elexis.jpa.StoreToStringService;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Stock;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.StockEntry;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.StockEntry_;
//import info.elexis.server.core.connector.elexis.locking.LockServiceInstance;
//import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;
//
//public class StockService extends PersistenceService implements IStockService {
//
//	private Logger log = LoggerFactory.getLogger(StockService.class);
//
//	public static class Builder extends AbstractBuilder<Stock> {
//		public Builder(String code, int priority) {
//			object = new Stock();
//			object.setCode(code);
//			object.setPriority(priority);
//		}
//	}
//
//	/**
//	 * convenience method
//	 * 
//	 * @param id
//	 * @return
//	 */
//	public static Optional<Stock> load(String id) {
//		return PersistenceService.load(Stock.class, id).map(v -> (Stock) v);
//	}
//
//	public static List<Stock> findAll(boolean includeElementsMarkedDeleted) {
//		return PersistenceService.findAll(Stock.class, includeElementsMarkedDeleted).stream().map(v -> (Stock) v)
//				.collect(Collectors.toList());
//	}
//
//	@Override
//	public IStockEntry storeArticleInStock(IStock stock, String storeToString) {
//		return storeArticleInStock(stock, storeToString, null);
//	}
//
//	public IStockEntry storeArticleInStock(IStock stock, String storeToString, Integer currentStock) {
//		IStockEntry stockEntry = findStockEntryForArticleInStock((Stock) stock, storeToString);
//		if (stockEntry != null) {
//			return stockEntry;
//		}
//		Optional<AbstractDBObjectIdDeleted> article = StoreToStringService.INSTANCE
//				.createDetachedFromString(storeToString);
//		if (!article.isPresent()) {
//			return null;
//		}
//
//		StockEntry se = new StockEntryService.Builder((Stock) stock, article.get()).build();
//		if (currentStock != null) {
//			se.setCurrentStock(currentStock);
//		}
//		return (IStockEntry) StockEntryService.save(se);
//	}
//
//	@Override
//	public void unstoreArticleFromStock(IStock stock, String storeToString) {
//		IStockEntry stockEntry = findStockEntryForArticleInStock((Stock) stock, storeToString);
//		if (stockEntry == null) {
//			return;
//		}
//		StockEntryService.remove((StockEntry) stockEntry);
//	}
//
//	@Override
//	public Integer getCumulatedStockForArticle(IArticle article) {
//		if (article == null) {
//			return null;
//		}
//		String storeToString = StoreToStringService.storeToString((AbstractDBObjectIdDeleted) article);
//		String[] typeId = info.elexis.server.core.service.StoreToStringService.splitIntoTypeAndId(storeToString);
//		EntityManager em = ElexisEntityManager.createEntityManager();
//		try {
//			Query stockCount = em.createNamedQuery(QueryConstants.QUERY_STOCK_ENTRY_findCummulatedStockSumOfArticle);
//			stockCount.setParameter(QueryConstants.PARAM_ARTICLE_TYPE, typeId[0]);
//			stockCount.setParameter(QueryConstants.PARAM_ARTICLE_ID, typeId[1]);
//			Long result = (Long) stockCount.getSingleResult();
//			if (result != null) {
//				return result.intValue();
//			}
//			return null;
//		} finally {
//			em.close();
//		}
//	}
//
//	@Override
//	public Availability getCumulatedAvailabilityForArticle(IArticle article) {
//		String storeToString = StoreToStringService.storeToString((AbstractDBObjectIdDeleted) article);
//		String[] typeId = info.elexis.server.core.service.StoreToStringService.splitIntoTypeAndId(storeToString);
//		EntityManager em = ElexisEntityManager.createEntityManager();
//		try {
//			Query stockCount = em
//					.createNamedQuery(QueryConstants.QUERY_STOCK_ENTRY_findCummulatedAvailabilityOfArticle);
//			stockCount.setParameter(QueryConstants.PARAM_ARTICLE_TYPE, typeId[0]);
//			stockCount.setParameter(QueryConstants.PARAM_ARTICLE_ID, typeId[1]);
//			Object result = stockCount.getSingleResult();
//			if (result == null) {
//				return null;
//			}
//
//			int value;
//
//			if (result instanceof Long) {
//				// MySQL
//				value = ((Long) result).intValue();
//			} else {
//				// h2
//				value = (int) result;
//			}
//
//			if (value > 1) {
//				return Availability.IN_STOCK;
//			} else if (value == 1) {
//				return Availability.CRITICAL_STOCK;
//			}
//			return Availability.OUT_OF_STOCK;
//		} finally {
//			em.close();
//		}
//	}
//
//	@Override
//	public Availability getArticleAvailabilityForStock(IStock stock, String article) {
//		IStockEntry se = findStockEntryForArticleInStock(stock, article);
//		return IStockService.determineAvailability(se.getCurrentStock(), se.getMinimumStock());
//	}
//
//	@Override
//	public List<? extends IStockEntry> findAllStockEntriesForArticle(String storeToString) {
//		String[] vals = storeToString.split(StringConstants.DOUBLECOLON);
//		JPAQuery<StockEntry> qre = new JPAQuery<StockEntry>(StockEntry.class);
//		qre.add(StockEntry_.articleType, QUERY.EQUALS, vals[0]);
//		qre.add(StockEntry_.articleId, QUERY.EQUALS, vals[1]);
//		return qre.execute();
//	}
//
//	@Override
//	public List<StockEntry> findAllStockEntriesForStock(IStock stock) {
//		JPAQuery<StockEntry> qre = new JPAQuery<StockEntry>(StockEntry.class);
//		qre.add(StockEntry_.stock, QUERY.EQUALS, stock);
//		return qre.execute();
//	}
//
//	@Override
//	public IStockEntry findPreferredStockEntryForArticle(String storeToString, String mandatorId) {
//		List<? extends IStockEntry> entries = findAllStockEntriesForArticle(storeToString);
//		int val = Integer.MAX_VALUE;
//		IStockEntry ret = null;
//		for (IStockEntry iStockEntry : entries) {
//			Stock stock = (Stock) iStockEntry.getStock();
//			Integer priority = stock.getPriority();
//			if (priority < val) {
//				val = priority;
//				ret = iStockEntry;
//			}
//			if (mandatorId != null) {
//				Kontakt owner = stock.getOwner();
//				if (owner != null && owner.getId().equals(mandatorId)) {
//					return iStockEntry;
//				}
//			}
//		}
//		return ret;
//	}
//
//	@Override
//	public IStockEntry findStockEntryForArticleInStock(IStock stock, String article) {
//		String[] vals = article.split(StringConstants.DOUBLECOLON);
//		JPAQuery<StockEntry> qre = new JPAQuery<StockEntry>(StockEntry.class);
//		qre.add(StockEntry_.stock, QUERY.EQUALS, ((Stock) stock));
//		qre.add(StockEntry_.articleType, QUERY.EQUALS, vals[0]);
//		qre.add(StockEntry_.articleId, QUERY.EQUALS, vals[1]);
//		List<StockEntry> qbe = qre.execute();
//		if (qbe.isEmpty()) {
//			return null;
//		}
//		return qbe.get(0);
//	}
//
//	public Optional<StockEntry> findStockEntryByGTINForStock(IStock stock, String gtin) {
//		Optional<? extends IArticle> findByGTIN = new ArticleService().findAnyByGTIN(gtin);
//		if (findByGTIN.isPresent()) {
//			String storeToString = StoreToStringService.storeToString((AbstractDBObjectIdDeleted) findByGTIN.get());
//			IStockEntry stockEntry = findStockEntryForArticleInStock(stock, storeToString);
//			return Optional.ofNullable((StockEntry) stockEntry);
//		}
//		return Optional.empty();
//	}
//
//	/**
//	 * Perform a disposal of a stock article. Takes into account whether
//	 * 
//	 * @param article
//	 * @param count
//	 * @param mandatorId
//	 * @return
//	 * @since 1.5
//	 */
//	public IStatus performDisposal(IArticle article, float count, String mandatorId) {
//		if (article == null) {
//			return new Status(Status.ERROR, BundleConstants.BUNDLE_ID, "Article is null");
//		}
//
//		String storeToString = StoreToStringService.storeToString((AbstractDBObjectIdDeleted) article);
//		IStockEntry se = findPreferredStockEntryForArticle(storeToString, mandatorId);
//		if (se == null) {
//			return new Status(Status.WARNING, BundleConstants.BUNDLE_ID, "No stock entry for article found");
//		}
//
//		if (se.getStock().isCommissioningSystem()) {
//			int outputCount;
//			if (count % 1 != 0) {
//				boolean performPartialOutly = ConfigService.INSTANCE.get(
//						Preferences.INVENTORY_MACHINE_OUTLAY_PARTIAL_PACKAGES,
//						Preferences.INVENTORY_MACHINE_OUTLAY_PARTIAL_PACKAGES_DEFAULT);
//				outputCount = (int) ((performPartialOutly) ? Math.ceil(count) : Math.floor(count));
//			} else {
//				outputCount = (int) count;
//			}
//
//			return new StockCommissioningSystemService().performArticleOutlay(se, outputCount, null);
//		} else {
//			Optional<LockInfo> li = LockServiceInstance.INSTANCE.acquireLockBlocking((StockEntry) se);
//			if (li.isPresent()) {
//				modifyStockCount(se, count * -1);
//
//				LockServiceInstance.INSTANCE.releaseLock(li.get());
//				return Status.OK_STATUS;
//			}
//
//			return new Status(Status.WARNING, BundleConstants.BUNDLE_ID, "Could not acquire lock");
//		}
//	}
//
//	@Override
//	public IStatus performSingleDisposal(IArticle article, int count, String mandatorId) {
//		return performDisposal(article, (float) count, mandatorId);
//	}
//
//	@Override
//	public IStatus performSingleReturn(IArticle article, int count, String mandatorId) {
//		if (article == null) {
//			return new Status(Status.ERROR, BundleConstants.BUNDLE_ID, "Article is null");
//		}
//
//		String storeToString = StoreToStringService.storeToString((AbstractDBObjectIdDeleted) article);
//		IStockEntry se = findPreferredStockEntryForArticle(storeToString, null);
//		if (se == null) {
//			return new Status(Status.WARNING, BundleConstants.BUNDLE_ID, "No stock entry for article found");
//		}
//
//		if (se.getStock().isCommissioningSystem()) {
//			// updates must happen via manual inputs in the machine
//			return Status.OK_STATUS;
//		}
//
//		Optional<LockInfo> li = LockServiceInstance.INSTANCE.acquireLockBlocking((StockEntry) se);
//		if (li.isPresent()) {
//			modifyStockCount(se, count);
//			
//			LockServiceInstance.INSTANCE.releaseLock(li.get());
//			return Status.OK_STATUS;
//		}
//
//		return new Status(Status.WARNING, BundleConstants.BUNDLE_ID, "Could not acquire lock");
//	}
//
//	/**
//	 * Modify the stock count for a given {@link IStockEntry}.<br>
//	 * The float value is split into base and mantissa, where base is the number of
//	 * full packages inserted (positive value) or dispensed (negative value). <br>
//	 * The mantissa is treated as a percentage value to the full package size of the
//	 * article (if available). If the package size of the article can not be
//	 * determined, the action is performed on the rounded full unit.
//	 * 
//	 * @param stockEntry
//	 * @param count
//	 * @since 1.5
//	 */
//	public void modifyStockCount(IStockEntry stockEntry, float count) {
//		int fullUnitsOnStock = stockEntry.getCurrentStock();
//		int fractionUnitsOnStock = stockEntry.getFractionUnits();
//		int packageUnit = stockEntry.getArticle().getPackageUnit();
//		int fullUnits;
//		int fractionUnits;
//		if ((count % 1 != 0) && packageUnit == 0) {
//			// we can't correctly determine the fractions
//			float tmpCount = (count < 0) ? (int) Math.floor(count) : (int) Math.ceil(count);
//			fullUnits = Math.abs((int) tmpCount);
//			fractionUnits = 0;
//			log.warn("StockEntry [{}], cannot determine fraction [{}], will handle as [{}].",
//					((StockEntry) stockEntry).getId(), Float.toString(count), Integer.toString(fullUnits));
//			count = tmpCount;
//		} else {
//			fullUnits = Math.abs((int) count);
//			fractionUnits = (int) ((Math.abs(count) - fullUnits) * packageUnit);
//		}
//	
//		if (count > 0) {
//			// add to stock
//			if (fractionUnits + fractionUnitsOnStock >= packageUnit) {
//				fullUnits++;
//				fractionUnits -= (fullUnits * packageUnit);
//			}
//		} else if (count < 0) {
//			// remove from stock
//			if (fractionUnits >= fractionUnitsOnStock) {
//				if (fractionUnits > fractionUnitsOnStock) {
//					fullUnits++;
//				}
//			}
//			fullUnits *= -1;
//			fractionUnits *= -1;
//		}
//	
//		stockEntry.setCurrentStock(fullUnitsOnStock + fullUnits);
//		stockEntry.setFractionUnits(Math.abs(fractionUnitsOnStock + fractionUnits));
//		StockEntryService.save((StockEntry) stockEntry);
//	}
//
//}
