package info.elexis.server.core.connector.elexis.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.article.IArticle;
import ch.elexis.core.services.IStockService;
import info.elexis.server.core.connector.elexis.internal.BundleConstants;
import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.QueryConstants;
import info.elexis.server.core.connector.elexis.jpa.StoreToStringService;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Stock;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.StockEntry;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.StockEntry_;
import info.elexis.server.core.connector.elexis.locking.LockServiceInstance;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;

public class StockService extends PersistenceService implements IStockService {

	public static class Builder extends AbstractBuilder<Stock> {
		public Builder(String code, int priority) {
			object = new Stock();
			object.setCode(code);
			object.setPriority(priority);
		}
	}

	/**
	 * convenience method
	 * 
	 * @param id
	 * @return
	 */
	public static Optional<Stock> load(String id) {
		return PersistenceService.load(Stock.class, id).map(v -> (Stock) v);
	}

	public static List<Stock> findAll(boolean includeElementsMarkedDeleted) {
		return PersistenceService.findAll(Stock.class, includeElementsMarkedDeleted).stream().map(v -> (Stock) v)
				.collect(Collectors.toList());
	}

	@Override
	public IStockEntry storeArticleInStock(IStock stock, String storeToString) {
		return storeArticleInStock(stock, storeToString, null);
	}

	public IStockEntry storeArticleInStock(IStock stock, String storeToString, Integer currentStock) {
		IStockEntry stockEntry = findStockEntryForArticleInStock((Stock) stock, storeToString);
		if (stockEntry != null) {
			return stockEntry;
		}
		Optional<AbstractDBObjectIdDeleted> article = StoreToStringService.INSTANCE
				.createDetachedFromString(storeToString);
		if (!article.isPresent()) {
			return null;
		}

		StockEntry se = new StockEntryService.Builder((Stock) stock, article.get()).build();
		if (currentStock != null) {
			se.setCurrentStock(currentStock);
		}
		StockEntryService.save(se);

		return se;
	}

	@Override
	public void unstoreArticleFromStock(IStock stock, String storeToString) {
		IStockEntry stockEntry = findStockEntryForArticleInStock((Stock) stock, storeToString);
		if (stockEntry == null) {
			return;
		}
		StockEntryService.remove((StockEntry) stockEntry);
	}

	@Override
	public Integer getCumulatedStockForArticle(IArticle article) {
		String storeToString = StoreToStringService.storeToString((AbstractDBObjectIdDeleted) article);
		String[] typeId = StoreToStringService.splitIntoTypeAndId(storeToString);
		EntityManager em = ElexisEntityManager.createEntityManager();
		try {
			Query stockCount = em.createNamedQuery(QueryConstants.QUERY_STOCK_ENTRY_findCummulatedStockSumOfArticle);
			stockCount.setParameter(QueryConstants.PARAM_ARTICLE_TYPE, typeId[0]);
			stockCount.setParameter(QueryConstants.PARAM_ARTICLE_ID, typeId[1]);
			Long result = (Long) stockCount.getSingleResult();
			if (result != null) {
				return result.intValue();
			}
			return null;
		} finally {
			em.close();
		}
	}

	@Override
	public Availability getCumulatedAvailabilityForArticle(IArticle article) {
		String storeToString = StoreToStringService.storeToString((AbstractDBObjectIdDeleted) article);
		String[] typeId = StoreToStringService.splitIntoTypeAndId(storeToString);
		EntityManager em = ElexisEntityManager.createEntityManager();
		try {
			Query stockCount = em
					.createNamedQuery(QueryConstants.QUERY_STOCK_ENTRY_findCummulatedAvailabilityOfArticle);
			stockCount.setParameter(QueryConstants.PARAM_ARTICLE_TYPE, typeId[0]);
			stockCount.setParameter(QueryConstants.PARAM_ARTICLE_ID, typeId[1]);
			Object result = stockCount.getSingleResult();
			if (result == null) {
				return null;
			}

			int value;

			if (result instanceof Long) {
				// MySQL
				value = ((Long) result).intValue();
			} else {
				// h2
				value = (int) result;
			}

			if (value > 1) {
				return Availability.IN_STOCK;
			} else if (value == 1) {
				return Availability.CRITICAL_STOCK;
			}
			return Availability.OUT_OF_STOCK;
		} finally {
			em.close();
		}
	}

	@Override
	public Availability getArticleAvailabilityForStock(IStock stock, String article) {
		IStockEntry se = findStockEntryForArticleInStock(stock, article);
		return IStockService.determineAvailability(se.getCurrentStock(), se.getMinimumStock());
	}

	@Override
	public List<? extends IStockEntry> findAllStockEntriesForArticle(String storeToString) {
		String[] vals = storeToString.split(StringConstants.DOUBLECOLON);
		JPAQuery<StockEntry> qre = new JPAQuery<StockEntry>(StockEntry.class);
		qre.add(StockEntry_.articleType, QUERY.EQUALS, vals[0]);
		qre.add(StockEntry_.articleId, QUERY.EQUALS, vals[1]);
		return qre.execute();
	}

	@Override
	public List<StockEntry> findAllStockEntriesForStock(IStock stock) {
		JPAQuery<StockEntry> qre = new JPAQuery<StockEntry>(StockEntry.class);
		qre.add(StockEntry_.stock, QUERY.EQUALS, stock);
		return qre.execute();
	}

	@Override
	public IStockEntry findPreferredStockEntryForArticle(String storeToString, String mandatorId) {
		List<? extends IStockEntry> entries = findAllStockEntriesForArticle(storeToString);
		int val = Integer.MAX_VALUE;
		IStockEntry ret = null;
		for (IStockEntry iStockEntry : entries) {
			Stock stock = (Stock) iStockEntry.getStock();
			Integer priority = stock.getPriority();
			if (priority < val) {
				val = priority;
				ret = iStockEntry;
			}
			if (mandatorId != null) {
				Kontakt owner = stock.getOwner();
				if (owner != null && owner.getId().equals(mandatorId)) {
					return iStockEntry;
				}
			}
		}
		return ret;
	}

	@Override
	public IStockEntry findStockEntryForArticleInStock(IStock stock, String article) {
		String[] vals = article.split(StringConstants.DOUBLECOLON);
		JPAQuery<StockEntry> qre = new JPAQuery<StockEntry>(StockEntry.class);
		qre.add(StockEntry_.stock, QUERY.EQUALS, ((Stock) stock));
		qre.add(StockEntry_.articleType, QUERY.EQUALS, vals[0]);
		qre.add(StockEntry_.articleId, QUERY.EQUALS, vals[1]);
		List<StockEntry> qbe = qre.execute();
		if (qbe.isEmpty()) {
			return null;
		}
		return qbe.get(0);
	}

	public Optional<StockEntry> findStockEntryByGTINForStock(IStock stock, String gtin) {
		Optional<? extends IArticle> findByGTIN = new ArticleService().findAnyByGTIN(gtin);
		if (findByGTIN.isPresent()) {
			String storeToString = StoreToStringService.storeToString((AbstractDBObjectIdDeleted) findByGTIN.get());
			IStockEntry stockEntry = findStockEntryForArticleInStock(stock, storeToString);
			return Optional.ofNullable((StockEntry) stockEntry);
		}
		return Optional.empty();
	}

	@Override
	public IStatus performSingleDisposal(IArticle article, int count, String mandatorId) {
		if (article == null) {
			return new Status(Status.ERROR, BundleConstants.BUNDLE_ID, "Article is null");
		}

		String storeToString = StoreToStringService.storeToString((AbstractDBObjectIdDeleted) article);
		IStockEntry se = findPreferredStockEntryForArticle(storeToString, mandatorId);
		if (se == null) {
			return new Status(Status.WARNING, BundleConstants.BUNDLE_ID, "No stock entry for article found");
		}

		if (se.getStock().isCommissioningSystem()) {
			return new StockCommissioningSystemService().performArticleOutlay(se, count, null);
		} else {
			Optional<LockInfo> li = LockServiceInstance.INSTANCE.acquireLockBlocking((StockEntry) se);
			if (li.isPresent()) {
				int fractionUnits = se.getFractionUnits();
				int ve = article.getSellingUnit();
				int vk = article.getPackageUnit();

				if (vk == 0) {
					if (ve != 0) {
						vk = ve;
					}
				}
				if (ve == 0) {
					if (vk != 0) {
						ve = vk;
					}
				}
				int num = count * ve;
				int cs = se.getCurrentStock();
				if (vk == ve) {
					se.setCurrentStock(cs - count);

				} else {
					int rest = fractionUnits - num;
					while (rest < 0) {
						rest = rest + vk;
						se.setCurrentStock(cs - 1);
					}
					se.setFractionUnits(rest);
				}

				StockEntryService.save((StockEntry) se);

				LockServiceInstance.INSTANCE.releaseLock(li.get());
				return Status.OK_STATUS;
			}

			return new Status(Status.WARNING, BundleConstants.BUNDLE_ID, "Could not acquire lock");
		}
	}

	@Override
	public IStatus performSingleReturn(IArticle article, int count, String mandatorId) {
		if (article == null) {
			return new Status(Status.ERROR, BundleConstants.BUNDLE_ID, "Article is null");
		}

		String storeToString = StoreToStringService.storeToString((AbstractDBObjectIdDeleted) article);
		IStockEntry se = findPreferredStockEntryForArticle(storeToString, null);
		if (se == null) {
			return new Status(Status.WARNING, BundleConstants.BUNDLE_ID, "No stock entry for article found");
		}

		if (se.getStock().isCommissioningSystem()) {
			// updates must happen via manual inputs in the machine
			return Status.OK_STATUS;
		}

		Optional<LockInfo> li = LockServiceInstance.INSTANCE.acquireLockBlocking((StockEntry) se);
		if (li.isPresent()) {
			int fractionUnits = se.getFractionUnits();
			int ve = article.getSellingUnit();
			int vk = article.getPackageUnit();

			if (vk == 0) {
				if (ve != 0) {
					vk = ve;
				}
			}
			if (ve == 0) {
				if (vk != 0) {
					ve = vk;
				}
			}
			int num = count * ve;
			int cs = se.getCurrentStock();
			if (vk == ve) {
				se.setCurrentStock(cs + count);
			} else {
				int rest = fractionUnits + num;
				while (rest > vk) {
					rest = rest - vk;
					se.setCurrentStock(cs + 1);
				}
				se.setFractionUnits(rest);
			}

			StockEntryService.save((StockEntry) se);
			LockServiceInstance.INSTANCE.releaseLock(li.get());
			return Status.OK_STATUS;
		}

		return new Status(Status.WARNING, BundleConstants.BUNDLE_ID, "Could not acquire lock");
	}

}
