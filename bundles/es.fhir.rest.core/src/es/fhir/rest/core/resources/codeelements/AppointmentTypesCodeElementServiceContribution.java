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
import ch.elexis.core.types.AppointmentType;

@Component
public class AppointmentTypesCodeElementServiceContribution implements ICodeElementServiceContribution {

	@Reference
	private IAppointmentService appointmentService;

	@Override
	public String getSystem() {
		return "appointment-type";
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
		List<ICodeElement> elements = new ArrayList<ICodeElement>();

		List<String> types = appointmentService.getTypes();
		String defaultType = appointmentService.getType(AppointmentType.DEFAULT);

		// put default type to front
		types.remove(defaultType);
		types.add(0, defaultType);

		types.forEach(type -> elements
				.add(new TransientCodeElement(getSystem(), type.replaceAll(" ", "_").toUpperCase(), type)));

		return elements;
	}

}
