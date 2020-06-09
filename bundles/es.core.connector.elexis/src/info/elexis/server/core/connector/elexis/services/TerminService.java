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
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

@Deprecated
public class TerminService {

	public static List<IAppointment> findAllAppointments(){
		IQuery<IAppointment> query = CoreModelServiceHolder.get().getQuery(IAppointment.class);
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
		Stream<?> executeNativeQuery = CoreModelServiceHolder.get().executeNativeQuery(nativeQuery);
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
				ConfigServiceHolder.get().get("agenda/bereich/" + areaName + "/type", null);
			if (areaType != null && areaType.startsWith(AreaType.CONTACT.name())) {
				String contactId = areaType.substring(AreaType.CONTACT.name().length() + 1);
				return CoreModelServiceHolder.get().load(contactId, IContact.class);
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
		return new HashSet<>(
			ConfigServiceHolder.get().getAsList("agenda/bereiche", Collections.emptyList()));
	}
	
}
