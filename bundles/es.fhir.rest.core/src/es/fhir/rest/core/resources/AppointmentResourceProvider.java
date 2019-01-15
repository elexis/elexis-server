package es.fhir.rest.core.resources;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Appointment;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Schedule;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import es.fhir.rest.core.resources.util.QueryUtil;
import es.fhir.rest.core.resources.util.TerminUtil;
import info.elexis.server.core.common.converter.DateConverter;


@Component
public class AppointmentResourceProvider implements IFhirResourceProvider {

	private Logger log;
	private ResourceProviderUtil resourceProviderUtil;
	
	@Reference(target="("+IModelService.SERVICEMODELNAME+"=ch.elexis.core.model)")
	private IModelService modelService;
	
	@Reference
	private IFhirTransformerRegistry transformerRegistry;

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Appointment.class;
	}
	
	@Activate
	public void activate() {
		log = LoggerFactory.getLogger(getClass());
		resourceProviderUtil = new ResourceProviderUtil();
	}

	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Appointment, IAppointment> getTransformer() {
		return (IFhirTransformer<Appointment, IAppointment>) transformerRegistry.getTransformerFor(Appointment.class,
				IAppointment.class);
	}

	@Read
	public Appointment getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<IAppointment> appointment = modelService.load(idPart, IAppointment.class);
			if (appointment.isPresent()) {
				Optional<Appointment> fhirAppointment = getTransformer().getFhirObject(appointment.get());
				return fhirAppointment.get();
			}
		}
		return null;
	}

	/**
	 * Find all appointments within a certain date range; optionally allocate them
	 * to a set of areas
	 * 
	 * @param dateParam
	 * @param actors
	 * @return
	 */
	@Search
	public List<Appointment> findAppointmentsByDateAndOptionallyArea(
			@RequiredParam(name = Appointment.SP_DATE) DateRangeParam dateParam,
			@Description(shortDefinition = "Only supports Schedule/identifier") @OptionalParam(name = Appointment.SP_ACTOR) ReferenceOrListParam actors,
			@IncludeParam(allow = { "Appointment:patient" }) Set<Include> theIncludes) {
		IQuery<IAppointment> query = modelService.getQuery(IAppointment.class);

		// gesperrte termine sind keine Termine - es sind nicht verfügbare Slots
		// TODO configurable
		query.and(ModelPackage.Literals.IAPPOINTMENT__TYPE, COMPARATOR.NOT_EQUALS, "gesperrt");

		DateConverter dateConverter = new DateConverter();

		// TODO support minutes
		if (dateParam.getLowerBound() != null) {
			LocalDate dayParam = dateConverter.convertToLocalDate(dateParam.getLowerBound().getValue());
			COMPARATOR compare = QueryUtil.prefixParamToToQueryParam(dateParam.getLowerBound().getPrefix());
			query.and("tag", compare, dayParam);
		}
		if (dateParam.getUpperBound() != null) {
			LocalDate dayParam2 = dateConverter.convertToLocalDate(dateParam.getUpperBound().getValue());
			COMPARATOR compare2 = QueryUtil.prefixParamToToQueryParam(dateParam.getUpperBound().getPrefix());
			query.and("tag", compare2, dayParam2);
		}

		if (actors != null) {
			query.startGroup();
			List<ReferenceParam> areas = actors.getValuesAsQueryTokens();
			for (ReferenceParam areaParam : areas) {
				if (Schedule.class.getSimpleName().equals(areaParam.getResourceType())) {
					Optional<String> agendaAreaName = TerminUtil.resolveAgendaAreaByScheduleId(areaParam.getIdPart());
					if (!agendaAreaName.isPresent()) {
						log.warn("Invalid agenda area id [{}]", areaParam.getIdPart());
						continue;
					}
					query.or(ModelPackage.Literals.IAPPOINTMENT__SCHEDULE, COMPARATOR.EQUALS, agendaAreaName.get());
				}
			}
			query.andJoinGroups();
		}

		List<IAppointment> termine = query.execute();
		if (termine.isEmpty()) {
			return Collections.emptyList();
		}

		return termine.parallelStream().map(a -> getTransformer().getFhirObject(a, theIncludes).get())
				.collect(Collectors.toCollection(ArrayList::new));
	}

	@Update
	public MethodOutcome updateAppointment(@IdParam IdType theId, @ResourceParam Appointment appointment) {
		return resourceProviderUtil.updateResource(theId, getTransformer(), appointment, log);
	}

}
