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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import es.fhir.rest.core.resources.util.QueryUtil;
import es.fhir.rest.core.resources.util.TerminUtil;
import info.elexis.server.core.common.converter.DateConverter;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Termin;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Termin_;
import info.elexis.server.core.connector.elexis.services.JPAQuery;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;
import info.elexis.server.core.connector.elexis.services.TerminService;

@Component
public class AppointmentResourceProvider implements IFhirResourceProvider {

	private static Logger log = LoggerFactory.getLogger(AppointmentResourceProvider.class);

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Appointment.class;
	}

	private IFhirTransformerRegistry transformerRegistry;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFhirTransformerRegistry(IFhirTransformerRegistry transformerRegistry) {
		this.transformerRegistry = transformerRegistry;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Appointment, Termin> getTransformer() {
		return (IFhirTransformer<Appointment, Termin>) transformerRegistry.getTransformerFor(Appointment.class,
				Termin.class);
	}

	@Read
	public Appointment getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<Termin> appointment = TerminService.load(idPart);
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
		JPAQuery<Termin> query = new JPAQuery<>(Termin.class);

		DateConverter dateConverter = new DateConverter();

		// TODO support minutes
		if (dateParam.getLowerBound() != null) {
			LocalDate dayParam = dateConverter.convertToLocalDate(dateParam.getLowerBound().getValue());
			QUERY compare = QueryUtil.prefixParamToToQueryParam(dateParam.getLowerBound().getPrefix());
			query.add(Termin_.tag, compare, dayParam);
		}
		if (dateParam.getUpperBound() != null) {
			LocalDate dayParam2 = dateConverter.convertToLocalDate(dateParam.getUpperBound().getValue());
			QUERY compare2 = QueryUtil.prefixParamToToQueryParam(dateParam.getUpperBound().getPrefix());
			query.add(Termin_.tag, compare2, dayParam2);
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
					query.or(Termin_.bereich, QUERY.EQUALS, agendaAreaName.get());
				}
			}
			query.endGroup_And();
		}

		List<Termin> termine = query.execute();
		if (termine.isEmpty()) {
			return Collections.emptyList();
		}

		return termine.parallelStream().map(a -> getTransformer().getFhirObject(a, theIncludes).get())
				.collect(Collectors.toCollection(ArrayList::new));
	}

}
