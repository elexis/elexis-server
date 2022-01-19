package es.fhir.rest.core.resources;

import java.util.List;
import java.util.Objects;
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
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.StringParam;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.agenda.Area;
import ch.elexis.core.model.agenda.AreaType;
import ch.elexis.core.services.IAppointmentService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;

@Component
public class ScheduleResourceProvider implements IFhirResourceProvider<Schedule, String> {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	protected IModelService coreModelService;
	
	@Reference
	private IAppointmentService appointmentService;
	
	@Reference
	private IContextService contextService;
	
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
	public Schedule read(@IdParam IdType theId){
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<Schedule> fhirSchedule = getTransformer().getFhirObject(idPart);
			return fhirSchedule.get();
		}
		return null;
	}
	
	/**
	 * Find the schedule (area) associated to the given user contact
	 * 
	 * @param userid,
	 *            if <code>null</code> user is resolved via context
	 * @return
	 */
	@Search(queryName = "user-schedule")
	public Schedule searchUserSchedule(@OptionalParam(name = "userid") StringParam userid){
		IUser user = contextService.getActiveUser().orElse(null);
		if (userid != null) {
			user = coreModelService.load(userid.getValue(), IUser.class).orElse(null);
		}
		
		if (user != null) {
			IContact assignedContact = user.getAssignedContact();
			if (assignedContact != null) {
				Optional<Area> userContactArea = appointmentService.getAreas().stream()
					.filter(a -> a.getType() == AreaType.CONTACT)
					.filter(a -> Objects.equals(a.getContactId(), assignedContact.getId()))
					.findFirst();
				if (userContactArea.isPresent()) {
					Schedule schedule = getTransformer()
						.getFhirObject(DigestUtils.md5Hex(userContactArea.get().getName())).get();
					return schedule;
				}
			}
		}
		return null;
	}
	
	@Search
	public List<Schedule> search(){
		List<Schedule> areas = appointmentService.getAreas().stream()
			.map(area -> getTransformer().getFhirObject(DigestUtils.md5Hex(area.getName())))
			.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
		return areas;
	}
	
}
