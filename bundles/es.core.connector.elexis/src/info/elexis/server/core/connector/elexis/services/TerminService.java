package info.elexis.server.core.connector.elexis.services;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Termin;

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

}
