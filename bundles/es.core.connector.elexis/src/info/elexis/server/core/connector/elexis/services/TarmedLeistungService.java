package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.billable.IBillable;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarTarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedKumulation;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedKumulation_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung_;

public class TarmedLeistungService extends AbstractService<TarmedLeistung> {

	private static Logger log = LoggerFactory.getLogger(TarmedLeistungService.class);
	
	public static TarmedLeistungService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final TarmedLeistungService INSTANCE = new TarmedLeistungService();
	}

	private TarmedLeistungService() {
		super(TarmedLeistung.class);
	}

	public Optional<IBillable> getVerrechenbarFromCode(String code) {
		Optional<TarmedLeistung> tl = findFromCode(code, null);
		if(tl.isPresent()) {
			return Optional.of(new VerrechenbarTarmedLeistung(tl.get()));
		}
		
		log.error("TarmedLeistung "+code+" not found!");
		
		return Optional.empty();
	}

	public static String getExclusionsForTarmedLeistung(TarmedLeistung tl, LocalDate date) {
		if (date == null) {
			date = LocalDate.now();
		}

		JPAQuery<TarmedKumulation> query = new JPAQuery<TarmedKumulation>(TarmedKumulation.class);
		query.add(TarmedKumulation_.masterCode, JPAQuery.QUERY.EQUALS, tl.getCode());
		query.add(TarmedKumulation_.typ, JPAQuery.QUERY.EQUALS, TarmedKumulation.TYP_EXCLUSION);

		List<TarmedKumulation> exclusions = query.execute();
		if (exclusions.isEmpty()) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		for (TarmedKumulation excl : exclusions) {
			if (isValidTarmedKumulation(excl, new TimeTool(date))) {
				if (!sb.toString().isEmpty()) {
					sb.append(",");
				}
				sb.append(excl.getSlaveCode());
			}
		}
		return sb.toString();
	}

	/**
	 * Checks if the kumulation is still/already valid on the given date
	 * 
	 * @param date
	 *            on which it should be valid
	 * @return true if valid, false otherwise
	 */
	private static boolean isValidTarmedKumulation(TarmedKumulation kumul, TimeTool date) {
		TimeTool from = new TimeTool(kumul.getValidFrom());
		TimeTool to = new TimeTool(kumul.getValidTo());

		if (date.isAfterOrEqual(from) && date.isBeforeOrEqual(to)) {
			return true;
		}
		return false;
	}

	public static Optional<TarmedLeistung> findFromCode(String code) {
		return findFromCode(code, null);
	}

	public static Optional<TarmedLeistung> findFromCode(String code, TimeTool date) {
		if (date == null) {
			date = new TimeTool();
		}
		JPAQuery<TarmedLeistung> query = new JPAQuery<TarmedLeistung>(TarmedLeistung.class);
		query.add(TarmedLeistung_.code, JPAQuery.QUERY.LIKE, code);
		List<TarmedLeistung> leistungen = query.execute();
		for (TarmedLeistung tarmedLeistung : leistungen) {
			TimeTool validFrom = new TimeTool(tarmedLeistung.getGueltigVon());
			LocalDate validToL = tarmedLeistung.getGueltigBis();
			TimeTool validTo = new TimeTool((validToL != null) ? validToL : LocalDate.of(2999, 12, 31));
			if (date.isAfterOrEqual(validFrom) && date.isBeforeOrEqual(validTo))
				return Optional.of(tarmedLeistung);
		}
		return Optional.empty();
	}
}
