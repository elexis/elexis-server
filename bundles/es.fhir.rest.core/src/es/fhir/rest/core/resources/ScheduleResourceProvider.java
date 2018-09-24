package es.fhir.rest.core.resources;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Schedule;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import info.elexis.server.core.connector.elexis.services.TerminService;

@Component
public class ScheduleResourceProvider implements IFhirResourceProvider {
	
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
	public Schedule getResourceById(@IdParam IdType theId){
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<Schedule> fhirSchedule = getTransformer().getFhirObject(idPart);
			return fhirSchedule.get();
		}
		return null;
	}
	
	@Search
	public List<Schedule> findSchedules(){
		Set<String> agendaAreas = TerminService.getAgendaAreas();
		List<Schedule> areas = agendaAreas.stream()
			.map(areaName -> getTransformer().getFhirObject(DigestUtils.md5Hex(areaName)))
			.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
		return areas;
	}
}
