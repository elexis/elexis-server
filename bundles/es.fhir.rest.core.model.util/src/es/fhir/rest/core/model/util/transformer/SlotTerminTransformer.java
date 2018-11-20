package es.fhir.rest.core.model.util.transformer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Schedule;
import org.hl7.fhir.dstu3.model.Slot;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.services.IModelService;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.model.util.transformer.helper.IAppointmentHelper;
import es.fhir.rest.core.resources.util.TerminUtil;

@Component
public class SlotTerminTransformer implements IFhirTransformer<Slot, IAppointment> {

	@org.osgi.service.component.annotations.Reference(target="("+IModelService.SERVICEMODELNAME+"=ch.elexis.core.model)")
	private IModelService modelService;
	
	private IAppointmentHelper appointmentHelper;

	@Activate
	private void activate() {
		appointmentHelper = new IAppointmentHelper();
	}
	
	@Override
	public Optional<Slot> getFhirObject(IAppointment localObject, Set<Include> includes) {
		Slot slot = new Slot();

		slot.setId(new IdDt(Slot.class.getSimpleName(), localObject.getId()));

		slot.setSchedule(new Reference(
				new IdType(Schedule.class.getSimpleName(), TerminUtil.getIdForBereich(localObject.getSchedule()))));

		slot.setStatus(appointmentHelper.getSlotStatus(localObject));

		LocalDateTime start = localObject.getStart();
		if(start != null) {
			Date start_ = Date.from(ZonedDateTime.of(start, ZoneId.systemDefault()).toInstant());
			slot.setStart(start_);
		} else {
			// TODO is required - what now?
		}
		
		LocalDateTime end = localObject.getEnd();
		if(end != null) {
			Date end_ = Date.from(ZonedDateTime.of(end, ZoneId.systemDefault()).toInstant());
			slot.setEnd(end_);
		} else {
			// TODO is required - what now?
		}

		return Optional.of(slot);
	}

	@Override
	public Optional<IAppointment> getLocalObject(Slot fhirObject) {
		String id = fhirObject.getIdElement().getIdPart();
		if (id != null && !id.isEmpty()) {
			return modelService.load(id, IAppointment.class);
		}
		return Optional.empty();
	}

	@Override
	public Optional<IAppointment> updateLocalObject(Slot fhirObject, IAppointment localObject) {
		return Optional.empty();
	}

	@Override
	public Optional<IAppointment> createLocalObject(Slot fhirObject) {
		return Optional.empty();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Slot.class.equals(fhirClazz) && IAppointment.class.equals(localClazz);
	}

}
