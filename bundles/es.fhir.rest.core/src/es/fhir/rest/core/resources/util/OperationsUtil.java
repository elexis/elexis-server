package es.fhir.rest.core.resources.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.core.runtime.IStatus;
import org.hl7.fhir.r4.model.OperationOutcome;

import ca.uhn.fhir.rest.server.exceptions.PreconditionFailedException;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.utils.OsgiServiceUtil;
import es.fhir.rest.core.resources.ResourceProviderUtil;

public class OperationsUtil {

	/**
	 * 
	 * @param coreModelService
	 * @param appointmentId
	 * @param patientId
	 * @param mandatorId
	 * @return
	 * @since 3.10
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static OperationOutcome handlePrintAppointmentsCard(IModelService coreModelService, String appointmentId,
			String patientId, String mandatorId) {

		Function printFunction = OsgiServiceUtil
				.getService(Function.class, "(service.name=ch.itmed.fop.printing.fhir.PrintAppointmentCardFunction)")
				.orElseThrow(() -> new PreconditionFailedException("service not available"));

		IAppointment _appointment = (appointmentId != null)
				? coreModelService.load(appointmentId, IAppointment.class).orElse(null)
				: null;
		IPatient _patient = null;
		if (patientId != null) {
			_patient = coreModelService.load(patientId, IPatient.class)
					.orElseThrow(() -> new PreconditionFailedException("invalid patient"));
		} else {
			if (_appointment != null) {
				IContact contact = _appointment.getContact();
				if (contact != null) {
					_patient = contact.asIPatient();
				}
			}
		}
		IMandator _mandator = null;
		if (mandatorId != null) {
			_mandator = coreModelService.load(mandatorId, IMandator.class)
					.orElseThrow(() -> new PreconditionFailedException("invalid mandator"));
		}

		if (_patient == null) {
			throw new PreconditionFailedException("invalid patient");
		}

		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("appointment", _appointment);
		paramMap.put("patient", _patient);
		paramMap.put("mandator", _mandator);

		IStatus status = (IStatus) printFunction.apply(paramMap);
		return ResourceProviderUtil.statusToOperationOutcome(status);
	}

}
