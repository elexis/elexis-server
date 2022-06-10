package es.fhir.rest.core.resources.codeelements;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.agenda.Area;
import ch.elexis.core.services.IAppointmentService;
import ch.elexis.core.services.ICodeElementService.CodeElementTyp;
import ch.elexis.core.services.ICodeElementServiceContribution;

@Component
public class ScheduleBlockedTimesConfigurationCodeElementServiceContribution
		implements ICodeElementServiceContribution {

	@Reference
	private IAppointmentService appointmentService;

	@Override
	public String getSystem() {
		return "schedule-blocked-times";
	}

	@Override
	public CodeElementTyp getTyp() {
		return CodeElementTyp.CONFIG;
	}

	@Override
	public Optional<ICodeElement> loadFromCode(String code, Map<Object, Object> context) {
		return Optional.empty();
	}

	@Override
	public List<ICodeElement> getElements(Map<Object, Object> context) {
		String scheduleNameOrId = (String) context.get("path");

		if (StringUtils.isNotBlank(scheduleNameOrId)) {
			Area areaByNameOrId = appointmentService.getAreaByNameOrId(scheduleNameOrId);
			if (areaByNameOrId != null) {
				List<ICodeElement> elements = new ArrayList<ICodeElement>();

				Map<DayOfWeek, String[]> blockTimes = appointmentService
						.getConfiguredBlockTimesBySchedule(areaByNameOrId.getName());
				blockTimes.entrySet().forEach(entry -> {
					TransientCodeElement codeElement = new TransientCodeElement(getSystem(), entry.getKey().name(),
							String.join(",", entry.getValue()));
					elements.add(codeElement);
				});

				return elements;
			}
		}

		return null;
	}

}
