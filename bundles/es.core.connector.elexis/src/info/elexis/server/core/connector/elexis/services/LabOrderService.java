package info.elexis.server.core.connector.elexis.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ch.elexis.core.model.ILabOrder;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

@Deprecated
public class LabOrderService {

	/**
	 * Find the LabOrder for a given LabResult, does not consider whether this
	 * LabOrder was marked deleted
	 * 
	 * @param labresult
	 * @return
	 */
	public static Optional<ILabOrder> findLabOrderByLabResult(ILabResult labResult) {
		IQuery<ILabOrder> query = CoreModelServiceHolder.get().getQuery(ILabOrder.class, true);
		query.and(ModelPackage.Literals.ILAB_ORDER__RESULT, COMPARATOR.EQUALS, labResult);
		return query.executeSingleResult();
	}

	/**
	 * Find all {@link LabOrder} that are equal to the provided
	 * {@link LabOrder#getOrderid()} and {@link LabOrder#getPatient()}
	 * 
	 * @param labOrder
	 * @return
	 */
	public static List<ILabOrder> findAllLabOrdersInSameOrderIdGroupWithResults(ILabOrder labOrder) {
		IQuery<ILabOrder> query = CoreModelServiceHolder.get().getQuery(ILabOrder.class);
		query.and(ModelPackage.Literals.ILAB_ORDER__PATIENT, COMPARATOR.EQUALS, labOrder.getPatient());
		query.and(ModelPackage.Literals.ILAB_ORDER__ORDER_ID, COMPARATOR.EQUALS, labOrder.getOrderId());
		query.and(ModelPackage.Literals.ILAB_ORDER__RESULT, COMPARATOR.NOT_EQUALS, null);
		return query.execute();
	}

	/**
	 * Find all {@link LabResult} entries for a given {@link LabOrder} id group.
	 * Excludes {@link LabResult} marked as deleted.
	 * 
	 * @param labOrder
	 * @return
	 */
	public static List<ILabResult> findAllLabResultsForLabOrderIdGroup(ILabOrder labOrder) {
		List<ILabOrder> ordersWithResult = findAllLabOrdersInSameOrderIdGroupWithResults(labOrder);
		return ordersWithResult.stream().map(owr -> owr.getResult()).filter(result -> !result.isDeleted())
				.collect(Collectors.toList());
	}
}
