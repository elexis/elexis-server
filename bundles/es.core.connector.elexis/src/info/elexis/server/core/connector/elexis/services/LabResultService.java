package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import ch.elexis.core.types.LabItemTyp;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabOrder;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult;

public class LabResultService extends AbstractService<LabResult> {
	public static LabResultService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final LabResultService INSTANCE = new LabResultService();
	}

	private LabResultService() {
		super(LabResult.class);
	}

	/**
	 * Determine the result for a LabResult - the result may
	 * not yet have been calculated. For formulas, this method handles
	 * calculation (if executable), for others it just passes through to the to
	 * {@link LabResult#getResult()}
	 */
	public String getInterpretedLabResult(LabResult lr) {
		if (lr.getItem() != null && LabItemTyp.FORMULA == lr.getItem().getTyp()) {
			String value = null;

			Optional<LabOrder> order = LabOrderService.findLabOrderByLabResult(lr);
			if (order.isPresent()) {
				List<LabResult> labresults = LabOrderService.findAllLabResultsForLabOrder(order.get());
				value = LabItemService.evaluate(order.get().getItem(), lr.getPatient(), labresults);
			}
			if (value == null || value.equals("?formel?")) { //$NON-NLS-1$
				LocalDateTime time = lr.getTransmissiontime();
				if (time == null) {
					time = lr.getObservationtime();
				}
				return LabItemService.evaluate(lr.getItem(), lr.getPatient(), new TimeTool(time));
			}
			return value;
		}

		return lr.getResult();
	}

}
