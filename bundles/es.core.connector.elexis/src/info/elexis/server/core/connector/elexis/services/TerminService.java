package info.elexis.server.core.connector.elexis.services;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.agenda.AreaType;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.utils.OsgiServiceUtil;

public class TerminService extends PersistenceService2 {
	
	private static IModelService modelService = OsgiServiceUtil.getService(IModelService.class,
		"(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();
	private static IConfigService configService =
		OsgiServiceUtil.getService(IConfigService.class).get();
	
//	public static class Builder extends AbstractBuilder<IAppointment> {
//		public Builder(String agendaMap, LocalDateTime begin, LocalDateTime end){
//			super(modelService)
//			object = modelService.create(IAppointment.class);
//			object.setSchedule(agendaMap);
//			object.setStart(begin);
//			object.setEnd(end);
//			object.setAngelegt(Long.toString(Instant.now().getEpochSecond() / 60));
//		}
//	}
	
	public static List<IAppointment> findAllAppointments(){
		IQuery<IAppointment> query = modelService.getQuery(IAppointment.class);
		query.and("id", COMPARATOR.NOT_EQUALS, "1");
		query.and(ModelPackage.Literals.IAPPOINTMENT__SCHEDULE, COMPARATOR.NOT_EQUALS, null);
		query.and("begin", COMPARATOR.NOT_EQUALS, null);
		query.and("duration", COMPARATOR.NOT_EQUALS, null);
		return query.execute();
	}
	
	/**
	 * 
	 * @return a list of all appointment arease ("Bereiche") used within the Agenda
	 */
	public static List<String> findAllUsedAppointmentAreas(){
		String nativeQuery = "SELECT DISTINCT BEREICH FROM AGNTERMINE WHERE BEREICH IS NOT NULL";
		Stream<?> executeNativeQuery = modelService.executeNativeQuery(nativeQuery);
		return executeNativeQuery.map(e -> e.toString()).collect(Collectors.toList());
	}
	
	/**
	 * Resolves the contact this appointment is allocated to. That is: If the appointment is part of
	 * a contact allocated area, the respective allocated contact is considered as allocated to.
	 * 
	 * @param localObject
	 * @return
	 * @see AreaType
	 * @since 1.6
	 */
	public static Optional<IContact> resolveAssignedContact(String areaName){
		if (areaName != null) {
			String areaType =
					configService.get("agenda/bereich/" + areaName + "/type", null);
			if (areaType != null && areaType.startsWith(AreaType.CONTACT.name())) {
				String contactId = areaType.substring(AreaType.CONTACT.name().length() + 1);
				return modelService.load(contactId, IContact.class);
			}
		}
		
		return Optional.empty();
	}
	
	/**
	 * 
	 * @return all defined agenda areas
	 * @since 1.6
	 */
	public static Set<String> getAgendaAreas(){
		// TODO does currently not consider areas that are available in agntermine, but
		// not provided in bereiche (i.e. inactive areas)
		return new HashSet<>(configService.getAsList("agenda/bereiche", Collections.emptyList()));
	}
	
}
