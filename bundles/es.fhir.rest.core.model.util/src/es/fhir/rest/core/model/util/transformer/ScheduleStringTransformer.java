package es.fhir.rest.core.model.util.transformer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Schedule;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.dstu.resource.Condition.Location;
import ca.uhn.fhir.model.primitive.IdDt;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.model.util.transformer.helper.TerminHelper;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.TerminService;

@Component
public class ScheduleStringTransformer implements IFhirTransformer<Schedule, String> {

	private TerminHelper terminHelper = new TerminHelper();

	@Override
	public Optional<Schedule> getFhirObject(String localObject) {
		return Optional.ofNullable(getSchedules().get(localObject));
	}

	@Override
	public Optional<String> getLocalObject(Schedule fhirObject) {
		if (getSchedules().containsKey(fhirObject.getId())) {
			return Optional.of(getSchedules().get(fhirObject.getId()).getId());
		}
		return Optional.empty();
	}

	private Map<String, Schedule> getSchedules() {
		Map<String, Schedule> schedules = new HashMap<>();

		Set<String> agendaAreas = TerminService.getAgendaAreas();
		for (String area : agendaAreas) {
			Schedule schedule = new Schedule();
			String areaId = terminHelper.getIdForBereich(area);

			Optional<Kontakt> assignedContact = TerminService.resolveAssignedContact(area);
			Reference actor;
			if (assignedContact.isPresent() && assignedContact.get().isMandator()) {
				actor = new Reference(new IdDt(Practitioner.class.getSimpleName(), assignedContact.get().getId()));
			} else {
				actor = new Reference(
						new IdDt(Location.class.getSimpleName(), LocationStringTransformer.MAIN_LOCATION));
			}

			schedule.setId(new IdDt(Schedule.class.getSimpleName(), areaId));
			schedule.setComment(area);
			schedule.setActive(true);
			schedule.getActor().add(actor);
			schedules.put(areaId, schedule);
		}

		return schedules;
	}

	@Override
	public Optional<String> updateLocalObject(Schedule fhirObject, String localObject) {
		return Optional.empty();
	}

	@Override
	public Optional<String> createLocalObject(Schedule fhirObject) {
		return Optional.empty();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Schedule.class.equals(fhirClazz) && String.class.equals(localClazz);
	}

}
