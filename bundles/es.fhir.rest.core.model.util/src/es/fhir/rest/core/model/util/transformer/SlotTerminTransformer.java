package es.fhir.rest.core.model.util.transformer;

import java.util.Date;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Schedule;
import org.hl7.fhir.dstu3.model.Slot;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.primitive.IdDt;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.model.util.transformer.helper.TerminHelper;
import es.fhir.rest.core.resources.util.TerminUtil;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Termin;
import info.elexis.server.core.connector.elexis.services.TerminService;

@Component
public class SlotTerminTransformer implements IFhirTransformer<Slot, Termin> {

	private TerminHelper terminHelper = new TerminHelper();

	@Override
	public Optional<Slot> getFhirObject(Termin localObject) {
		Slot slot = new Slot();

		slot.setId(new IdDt(Slot.class.getSimpleName(), localObject.getId()));

		slot.setSchedule(new Reference(
				new IdType(Schedule.class.getSimpleName(), TerminUtil.getIdForBereich(localObject.getBereich()))));

		slot.setStatus(terminHelper.getSlotStatus(localObject));

		Optional<Object[]> startEndDuration = terminHelper.getStartEndDuration(localObject);
		if (startEndDuration.isPresent()) {
			slot.setStart((Date) startEndDuration.get()[0]);
			slot.setEnd((Date) startEndDuration.get()[1]);
		} else {
			// TODO is required - what now?
		}

		return Optional.of(slot);
	}

	@Override
	public Optional<Termin> getLocalObject(Slot fhirObject) {
		String id = fhirObject.getIdElement().getIdPart();
		if (id != null && !id.isEmpty()) {
			return TerminService.load(id);
		}
		return Optional.empty();
	}

	@Override
	public Optional<Termin> updateLocalObject(Slot fhirObject, Termin localObject) {
		return Optional.empty();
	}

	@Override
	public Optional<Termin> createLocalObject(Slot fhirObject) {
		return Optional.empty();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Slot.class.equals(fhirClazz) && Termin.class.equals(localClazz);
	}

}
