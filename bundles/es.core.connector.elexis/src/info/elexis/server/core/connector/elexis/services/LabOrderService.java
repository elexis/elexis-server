package info.elexis.server.core.connector.elexis.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabOrder;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabOrder_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;

public class LabOrderService extends AbstractService<LabOrder> {
	public static LabOrderService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final LabOrderService INSTANCE = new LabOrderService();
	}

	private LabOrderService() {
		super(LabOrder.class);
	}

	public static Optional<LabOrder> findLabOrderByLabResult(LabResult labresult) {
		JPAQuery<LabOrder> query = new JPAQuery<LabOrder>(LabOrder.class);
		query.add(LabOrder_.result, QUERY.EQUALS, labresult);
		return query.executeGetSingleResult();
	}

	public static List<LabOrder> findAllLabOrdersInSameOrderIdGroup(LabOrder labOrder) {
		JPAQuery<LabOrder> query = new JPAQuery<LabOrder>(LabOrder.class);
		query.add(LabOrder_.orderid, QUERY.EQUALS, labOrder.getOrderid());
		return query.execute();
	}
	
	public static List<LabOrder> findAllLabOrdersInSameOrderIdGroupWithResults(LabOrder labOrder) {
		JPAQuery<LabOrder> query = new JPAQuery<LabOrder>(LabOrder.class);
		query.add(LabOrder_.orderid, QUERY.EQUALS, labOrder.getOrderid());
		query.add(LabOrder_.result, QUERY.NOT_EQUALS, null);
		return query.execute();
	}

	public static List<LabResult> findAllLabResultsForLabOrderIdGroup(LabOrder labOrder) {
		List<LabOrder> ordersWithResult = findAllLabOrdersInSameOrderIdGroupWithResults(labOrder);
		return ordersWithResult.stream().map(owr->owr.getResult()).collect(Collectors.toList());
	}
}
