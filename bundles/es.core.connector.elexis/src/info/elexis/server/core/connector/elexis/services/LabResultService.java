package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ch.elexis.core.types.LabItemTyp;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabOrder;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult;

public class LabResultService extends PersistenceService {

	public static class Builder extends AbstractBuilder<LabResult> {
		public Builder(LabItem labItem, Kontakt patient) {
			object = new LabResult();
			object.setItem(labItem);
			object.setPatient(patient);
		}
	}

	/**
	 * convenience method
	 * 
	 * @param id
	 * @return
	 */
	public static Optional<LabResult> load(String id) {
		return PersistenceService.load(LabResult.class, id).map(v -> (LabResult) v);
	}

	/**
	 * convenience method
	 * 
	 * @param includeElementsMarkedDeleted
	 * @return
	 */
	public static List<LabResult> findAll(boolean includeElementsMarkedDeleted) {
		return PersistenceService.findAll(LabResult.class, includeElementsMarkedDeleted).stream()
				.map(v -> (LabResult) v).collect(Collectors.toList());
	}

	/**
	 * Determine the result for a LabResult - the result may not yet have been
	 * calculated. For formulas, this method handles calculation (if
	 * executable), for others it just passes through to the to
	 * {@link LabResult#getResult()}
	 */
	public String getInterpretedLabResult(LabResult lr) {
		if (lr.getItem() != null && LabItemTyp.FORMULA == lr.getItem().getTyp()) {
			String value = null;

			Optional<LabOrder> order = LabOrderService.findLabOrderByLabResult(lr);
			if (order.isPresent()) {
				List<LabResult> labresults = LabOrderService.findAllLabResultsForLabOrderIdGroup(order.get());
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
