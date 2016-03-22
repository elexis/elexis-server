package info.elexis.server.core.connector.elexis.services;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.List;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Termin;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Termin_;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;

public class TerminService extends AbstractService<Termin> {
	public static TerminService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final TerminService INSTANCE = new TerminService();
	}

	private TerminService() {
		super(Termin.class);
	}

	public Termin create(String agendaMap, LocalDateTime begin, LocalDateTime end) {
		em.getTransaction().begin();
		Termin t = create(false);
		t.setBereich(agendaMap);
		t.setTag(begin.toLocalDate());
		int beginn = begin.get(ChronoField.CLOCK_HOUR_OF_DAY) * 60 + begin.get(ChronoField.MINUTE_OF_HOUR);
		t.setBeginn(Integer.toString(beginn));
		Duration duration = Duration.between(begin, end);
		t.setDauer(Long.toString(duration.toMinutes()));
		t.setAngelegt(Long.toString(Instant.now().getEpochSecond() / 60));
		em.getTransaction().commit();
		return t;
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
	 * @return a list of all appointment arease ("Bereiche") used within the
	 *         Agenda
	 */
	public List<String> findAllUsedAppointmentAreas() {
		CriteriaQuery<String> query = cb.createQuery(String.class);
		Root<Termin> terminRoot = query.from(Termin.class);
		query.select(terminRoot.get(Termin_.bereich)).distinct(true);
		query.where(cb.and(cb.not(cb.isNull(terminRoot.get(Termin_.bereich)))));
		return em.createQuery(query).getResultList();
	}

}
