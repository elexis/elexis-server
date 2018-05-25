package info.elexis.server.core.connector.elexis.services;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import ch.elexis.core.model.agenda.AreaType;
import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Termin;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Termin_;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;

public class TerminService extends PersistenceService {
	public static class Builder extends AbstractBuilder<Termin> {
		public Builder(String agendaMap, LocalDateTime begin, LocalDateTime end) {
			object = new Termin();
			object.setBereich(agendaMap);
			object.setTag(begin.toLocalDate());
			int beginn = begin.get(ChronoField.CLOCK_HOUR_OF_DAY) * 60 + begin.get(ChronoField.MINUTE_OF_HOUR);
			object.setBeginn(Integer.toString(beginn));
			Duration duration = Duration.between(begin, end);
			object.setDauer(Long.toString(duration.toMinutes()));
			object.setAngelegt(Long.toString(Instant.now().getEpochSecond() / 60));
		}
	}

	/**
	 * convenience method
	 * 
	 * @param id
	 * @return
	 */
	public static Optional<Termin> load(String id) {
		return PersistenceService.load(Termin.class, id).map(v -> (Termin) v);
	}

	/**
	 * convenience method
	 * 
	 * @param includeElementsMarkedDeleted
	 * @return
	 */
	public static List<Termin> findAll(boolean includeElementsMarkedDeleted) {
		return PersistenceService.findAll(Termin.class, includeElementsMarkedDeleted).stream().map(v -> (Termin) v)
				.collect(Collectors.toList());
	}

	public static List<Termin> findAllAppointments() {
		JPAQuery<Termin> query = new JPAQuery<Termin>(Termin.class);
		query.add(AbstractDBObjectIdDeleted_.id, QUERY.NOT_EQUALS, "1");
		query.add(Termin_.bereich, QUERY.NOT_EQUALS, null);
		query.add(Termin_.beginn, QUERY.NOT_EQUALS, null);
		query.add(Termin_.dauer, QUERY.NOT_EQUALS, null);
		return query.execute();
	}

	/**
	 * 
	 * @return a list of all appointment arease ("Bereiche") used within the Agenda
	 */
	public static List<String> findAllUsedAppointmentAreas() {
		EntityManager em = ElexisEntityManager.createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<String> query = cb.createQuery(String.class);
			Root<Termin> terminRoot = query.from(Termin.class);
			query.select(terminRoot.get(Termin_.bereich)).distinct(true);
			query.where(cb.and(cb.not(cb.isNull(terminRoot.get(Termin_.bereich)))));
			return em.createQuery(query).getResultList();
		} finally {
			em.close();
		}
	}

	/**
	 * Resolves the contact this appointment is allocated to. That is: If the
	 * appointment is part of a contact allocated area, the respective allocated
	 * contact is considered as allocated to.
	 * 
	 * @param localObject
	 * @return
	 * @see AreaType
	 * @since 1.6
	 */
	public static Optional<Kontakt> resolveAssignedContact(String areaName) {
		if (areaName != null) {
			String areaType = ConfigService.INSTANCE.get("agenda/bereich/" + areaName + "/type", null);
			if (areaType != null && areaType.startsWith(AreaType.CONTACT.name())) {
				String contactId = areaType.substring(AreaType.CONTACT.name().length() + 1);
				return KontaktService.load(contactId);
			}
		}

		return Optional.empty();
	}

	/**
	 * 
	 * @return all defined agenda areas
	 * @since 1.6
	 */
	public static Set<String> getAgendaAreas() {
		// TODO does currently not consider areas that are available in agntermine, but
		// not provided in bereiche (i.e. inactive areas)
		return ConfigService.INSTANCE.getAsSet("agenda/bereiche");
	}

}
