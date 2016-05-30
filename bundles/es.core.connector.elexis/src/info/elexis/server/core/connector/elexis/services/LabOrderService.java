package info.elexis.server.core.connector.elexis.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

	public static List<LabResult> findAllLabResultsForLabOrder(LabOrder labOrder) {
		List<LabResult> ret = new ArrayList<LabResult>();
		List<LabOrder> orders = findAllLabOrdersInSameOrderIdGroup(labOrder);
		if (orders != null) {
			for (LabOrder order : orders) {
				if (order.getResult() != null) {
					ret.add(labOrder.getResult());
				}
			}
		}
		return ret;
	}
}
