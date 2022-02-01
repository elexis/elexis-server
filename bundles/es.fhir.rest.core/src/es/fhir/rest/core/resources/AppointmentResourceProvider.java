package es.fhir.rest.core.resources;

import java.time.LocalDate;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Appointment;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IAppointmentService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.time.DateConverter;
import es.fhir.rest.core.resources.util.QueryUtil;

@Component(service = IFhirResourceProvider.class)
public class AppointmentResourceProvider
		extends AbstractFhirCrudResourceProvider<Appointment, IAppointment> {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;
	
	@Reference
	private IAppointmentService appointmentService;
	
	@Reference
	private IFhirTransformerRegistry transformerRegistry;
	
	public AppointmentResourceProvider(){
		super(IAppointment.class);
	}
	
	@Override
	public Class<? extends IBaseResource> getResourceType(){
		return Appointment.class;
	}
	
	@Activate
	public void activate(){
		super.setCoreModelService(coreModelService);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Appointment, IAppointment> getTransformer(){
		return (IFhirTransformer<Appointment, IAppointment>) transformerRegistry
			.getTransformerFor(Appointment.class, IAppointment.class);
	}
	
	@Search
	public List<Appointment> search(
		@OptionalParam(name = Appointment.SP_DATE) DateRangeParam dateParam,
		@OptionalParam(name = Appointment.SP_SERVICE_CATEGORY) TokenParam serviceCategoryParam,
		@OptionalParam(name = Appointment.SP_PATIENT) StringParam patientParam){
		
		// TODO default limit, offset, paging
		
		IQuery<IAppointment> query = coreModelService.getQuery(IAppointment.class);
		// TODO configurable
		query.and(ModelPackage.Literals.IAPPOINTMENT__TYPE, COMPARATOR.NOT_EQUALS, "gesperrt");
		
		if (serviceCategoryParam != null && StringUtils.equalsIgnoreCase(
			CodingSystem.ELEXIS_AGENDA_AREA_ID.getSystem(), serviceCategoryParam.getSystem())) {
			query.and(ModelPackage.Literals.IAPPOINTMENT__SCHEDULE, COMPARATOR.EQUALS,
				serviceCategoryParam.getValue());
		}
		
		if (dateParam != null) {
			DateConverter dateConverter = new DateConverter();
			if (dateParam.getLowerBound() != null) {
				LocalDate dayParam =
					dateConverter.convertToLocalDate(dateParam.getLowerBound().getValue());
				COMPARATOR compare =
					QueryUtil.prefixParamToToQueryParam(dateParam.getLowerBound().getPrefix());
				query.and("tag", compare, dayParam);
			}
			if (dateParam.getUpperBound() != null) {
				LocalDate dayParam2 =
					dateConverter.convertToLocalDate(dateParam.getUpperBound().getValue());
				COMPARATOR compare2 =
					QueryUtil.prefixParamToToQueryParam(dateParam.getUpperBound().getPrefix());
				query.and("tag", compare2, dayParam2);
			}
		}
		
		if (patientParam != null) {
			String patientId = patientParam.getValue();
			query.and(ModelPackage.Literals.IAPPOINTMENT__SUBJECT_OR_PATIENT, COMPARATOR.EQUALS,
				patientId);
		}
		
		return super.handleExecute(query);
	}
	
//	/**
//	 * Find all appointments within a certain date range; optionally allocate them to a set of areas
//	 * 
//	 * @param dateParam
//	 * @param actors
//	 * @return
//	 */
//	@Search
//	public List<Appointment> findAppointmentsByDateAndOptionallyArea(
//		@RequiredParam(name = Appointment.SP_DATE) DateRangeParam dateParam,
//		@Description(shortDefinition = "Only supports Schedule/identifier")
//		@OptionalParam(name = Appointment.SP_ACTOR) ReferenceOrListParam actors,
//		@IncludeParam(allow = {
//			"Appointment:patient"
//		}) Set<Include> theIncludes){
//		IQuery<IAppointment> query = coreModelService.getQuery(IAppointment.class);
//		
//		
//		// TODO configurable
//		query.and(ModelPackage.Literals.IAPPOINTMENT__TYPE, COMPARATOR.NOT_EQUALS, "gesperrt");
//		
//		DateConverter dateConverter = new DateConverter();
//		
//		// TODO support minutes
//		if (dateParam.getLowerBound() != null) {
//			LocalDate dayParam =
//				dateConverter.convertToLocalDate(dateParam.getLowerBound().getValue());
//			COMPARATOR compare =
//				QueryUtil.prefixParamToToQueryParam(dateParam.getLowerBound().getPrefix());
//			query.and("tag", compare, dayParam);
//		}
//		if (dateParam.getUpperBound() != null) {
//			LocalDate dayParam2 =
//				dateConverter.convertToLocalDate(dateParam.getUpperBound().getValue());
//			COMPARATOR compare2 =
//				QueryUtil.prefixParamToToQueryParam(dateParam.getUpperBound().getPrefix());
//			query.and("tag", compare2, dayParam2);
//		}
//		
//		if (actors != null) {
//			query.startGroup();
//			List<ReferenceParam> areas = actors.getValuesAsQueryTokens();
//			for (ReferenceParam areaParam : areas) {
//				if (Schedule.class.getSimpleName().equals(areaParam.getResourceType())) {
//					Optional<String> agendaAreaName = new TerminUtil(appointmentService)
//						.resolveAgendaAreaByScheduleId(areaParam.getIdPart());
//					if (!agendaAreaName.isPresent()) {
//						log.warn("Invalid agenda area id [{}]", areaParam.getIdPart());
//						continue;
//					}
//					query.or(ModelPackage.Literals.IAPPOINTMENT__SCHEDULE, COMPARATOR.EQUALS,
//						agendaAreaName.get());
//				}
//			}
//			query.andJoinGroups();
//		}
//		
//		List<IAppointment> termine = query.execute();
//		if (termine.isEmpty()) {
//			return Collections.emptyList();
//		}
//		
//		return termine.parallelStream()
//			.map(a -> getTransformer().getFhirObject(a, SummaryEnum.FALSE, theIncludes).get())
//			.collect(Collectors.toList());
//	}
	
}
