package es.fhir.rest.core.model.util.transformer;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Appointment;
import org.hl7.fhir.dstu3.model.Appointment.AppointmentParticipantComponent;
import org.hl7.fhir.dstu3.model.Appointment.ParticipantRequired;
import org.hl7.fhir.dstu3.model.Appointment.ParticipationStatus;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Slot;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.primitive.IdDt;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.model.util.transformer.helper.TerminHelper;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Termin;
import info.elexis.server.core.connector.elexis.services.KontaktService;
import info.elexis.server.core.connector.elexis.services.TerminService;

@Component
public class AppointmentTerminTransformer implements IFhirTransformer<Appointment, Termin> {

	private TerminHelper terminHelper = new TerminHelper();

	@Override
	public Optional<Appointment> getFhirObject(Termin localObject) {
		Appointment appointment = new Appointment();

		appointment.setId(new IdDt(Appointment.class.getSimpleName(), localObject.getId()));

		appointment.setStatus(terminHelper.getStatus(localObject));

		appointment.setDescription(terminHelper.getDescription(localObject));

		Optional<Object[]> startEndDuration = terminHelper.getStartEndDuration(localObject);
		if (startEndDuration.isPresent()) {
			appointment.setStart((Date) startEndDuration.get()[0]);
			appointment.setEnd((Date) startEndDuration.get()[1]);
			appointment.setMinutesDuration((int) startEndDuration.get()[2]);
		}

		Reference slotReference = new Reference(new IdType(Slot.class.getSimpleName(), localObject.getId()));
		appointment.setSlot(Collections.singletonList(slotReference));

		List<AppointmentParticipantComponent> participant = appointment.getParticipant();

		Optional<Kontakt> assignedContact = TerminService.resolveAssignedContact(localObject.getBereich());
		if (assignedContact.isPresent() && assignedContact.get().isMandator()) {
			AppointmentParticipantComponent hcp = new AppointmentParticipantComponent();
			hcp.setActor(new Reference(new IdDt(Practitioner.class.getSimpleName(), assignedContact.get().getId())));
			hcp.setRequired(ParticipantRequired.REQUIRED);
			hcp.setStatus(ParticipationStatus.ACCEPTED);
			participant.add(hcp);
		}

		String patientIdOrSomeString = localObject.getPatId();
		if (patientIdOrSomeString != null) {
			Optional<Kontakt> contact = KontaktService.load(patientIdOrSomeString);
			if (contact.isPresent()) {
				AppointmentParticipantComponent patient = new AppointmentParticipantComponent();
				patient.setActor(new Reference(new IdDt(Patient.class.getSimpleName(), patientIdOrSomeString)));
				patient.setRequired(ParticipantRequired.REQUIRED);
				patient.setStatus(ParticipationStatus.ACCEPTED);
				participant.add(patient);
			} else {
				// TODO there is another string inside - where to put it? is it relevant?
			}
		}

		return Optional.of(appointment);
	}

	@Override
	public Optional<Termin> getLocalObject(Appointment fhirObject) {
		String id = fhirObject.getIdElement().getIdPart();
		if (id != null && !id.isEmpty()) {
			return TerminService.load(id);
		}
		return Optional.empty();
	}

	@Override
	public Optional<Termin> updateLocalObject(Appointment fhirObject, Termin localObject) {
		return Optional.empty();
	}

	@Override
	public Optional<Termin> createLocalObject(Appointment fhirObject) {
		return Optional.empty();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Appointment.class.equals(fhirClazz) && Termin.class.equals(localClazz);
	}

}
