package info.elexis.server.core.connector.elexis.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabOrder;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabOrder_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;

public class LabOrderService extends PersistenceService {
	/**
	 * convenience method
	 * 
	 * @param id
	 * @return
	 */
	public static Optional<LabOrder> load(String id) {
		return PersistenceService.load(LabOrder.class, id).map(v -> (LabOrder) v);
	}

	public static Optional<LabOrder> findLabOrderByLabResult(LabResult labresult) {
		JPAQuery<LabOrder> query = new JPAQuery<LabOrder>(LabOrder.class);
		query.add(LabOrder_.result, QUERY.EQUALS, labresult);
		return query.executeGetSingleResult();
	}

	/**
	 * Find all {@link LabOrder} that are equal to the provided {@link LabOrder#getOrderid()} and
	 * {@link LabOrder#getPatient()}
	 * 
	 * @param labOrder
	 * @return
	 */
	public static List<LabOrder> findAllLabOrdersInSameOrderIdGroupWithResults(LabOrder labOrder) {
		JPAQuery<LabOrder> query = new JPAQuery<LabOrder>(LabOrder.class);
		query.add(LabOrder_.patient, QUERY.EQUALS, labOrder.getPatient());
		query.add(LabOrder_.orderid, QUERY.EQUALS, labOrder.getOrderid());
		query.add(LabOrder_.result, QUERY.NOT_EQUALS, null);
		return query.execute();
	}

	/**
	 * Find all {@link LabResult} entries for a given {@link LabOrder} id group.
	 * Excludes {@link LabResult} marked as deleted.
	 * 
	 * @param labOrder
	 * @return
	 */
	public static List<LabResult> findAllLabResultsForLabOrderIdGroup(LabOrder labOrder) {
		List<LabOrder> ordersWithResult = findAllLabOrdersInSameOrderIdGroupWithResults(labOrder);
		return ordersWithResult.stream().map(owr -> owr.getResult()).filter(result -> !result.isDeleted())
				.collect(Collectors.toList());
	}
}
