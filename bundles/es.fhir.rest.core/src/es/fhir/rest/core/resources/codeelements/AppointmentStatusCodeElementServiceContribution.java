package es.fhir.rest.core.resources.codeelements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.services.IAppointmentService;
import ch.elexis.core.services.ICodeElementService.CodeElementTyp;
import ch.elexis.core.services.ICodeElementServiceContribution;
import ch.elexis.core.types.AppointmentState;

@Component
public class AppointmentStatusCodeElementServiceContribution implements ICodeElementServiceContribution {

	// TODO provide mapping to formal FHIR states?
	// TODO add color information per user?

	@Reference
	private IAppointmentService appointmentService;

	@Override
	public String getSystem() {
		return "appointment-status";
	}

	@Override
	public CodeElementTyp getTyp() {
		return CodeElementTyp.CONFIG;
	}

	@Override
	public Optional<ICodeElement> loadFromCode(String code, Map<Object, Object> context) {
		return null;
	}

	@Override
	public List<ICodeElement> getElements(Map<Object, Object> context) {
		ArrayList<ICodeElement> elements = new ArrayList<ICodeElement>();

		List<String> states = appointmentService.getStates();
		// this is not a valid selectable state, hence we do not hand it out
		states.remove(appointmentService.getState(AppointmentState.EMPTY));

		states.forEach(state -> elements
				.add(new TransientCodeElement(getSystem(), state.replaceAll(" ", "_").toUpperCase(), state)));

		return elements;
	}

}
