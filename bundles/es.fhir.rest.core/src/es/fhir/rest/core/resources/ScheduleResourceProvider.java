package es.fhir.rest.core.resources;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Schedule;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.agenda.Area;
import ch.elexis.core.services.IAppointmentService;

@Component
public class ScheduleResourceProvider implements IFhirResourceProvider {
	
	@Reference
	private IAppointmentService appointmentService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType(){
		return Schedule.class;
	}
	
	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC)
	private IFhirTransformerRegistry transformerRegistry;
	
	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Schedule, String> getTransformer(){
		return (IFhirTransformer<Schedule, String>) transformerRegistry
			.getTransformerFor(Schedule.class, String.class);
	}
	
	@Read
	public Schedule getResourceById(@IdParam
	IdType theId){
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<Schedule> fhirSchedule = getTransformer().getFhirObject(idPart);
			return fhirSchedule.get();
		}
		return null;
	}
	
	@Search
	public List<Schedule> findSchedules(){
		List<Area> agendaAreas = appointmentService.getAreas();
		List<Schedule> areas = agendaAreas.stream()
			.map(area -> getTransformer().getFhirObject(DigestUtils.md5Hex(area.getName())))
			.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
		return areas;
	}
}
