package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDate;
import java.util.List;

import ch.rgw.tools.Result;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.billable.IVerrechenbar;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarTarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedKumulation;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedKumulation_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung_;

public class TarmedLeistungService extends AbstractService<TarmedLeistung> {
	public static TarmedLeistungService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final TarmedLeistungService INSTANCE = new TarmedLeistungService();
	}

	private TarmedLeistungService() {
		super(TarmedLeistung.class);
	}

	public IVerrechenbar getVerrechenbarFromCode(String code) {
		TarmedLeistung tl = getFromCode(code);
		return new VerrechenbarTarmedLeistung(tl);
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
	private static boolean isValidTarmedKumulation(TarmedKumulation kumul, TimeTool date){
		TimeTool from = new TimeTool(kumul.getValidFrom());
		TimeTool to = new TimeTool(kumul.getValidTo());
		
		if (date.isAfterOrEqual(from) && date.isBeforeOrEqual(to)) {
			return true;
		}
		return false;
	}
	
	public TarmedLeistung getFromCode(String code) {
		JPAQuery<TarmedLeistung> qre = new JPAQuery<TarmedLeistung>(TarmedLeistung.class);
		qre.add(TarmedLeistung_.code, JPAQuery.QUERY.LIKE, code);
		return qre.executeGetSingleResult();
	}

	public static TarmedLeistung getFromCode(String code, TimeTool date) {		
		JPAQuery<TarmedLeistung> query = new JPAQuery<TarmedLeistung>(TarmedLeistung.class);
		query.add(TarmedLeistung_.code, JPAQuery.QUERY.LIKE, code);
		List<TarmedLeistung> leistungen = query.execute();
		for (TarmedLeistung tarmedLeistung : leistungen) {
			TimeTool validFrom = new TimeTool(tarmedLeistung.getGueltigVon());
			TimeTool validTo = new TimeTool(tarmedLeistung.getGueltigBis());
			if (date.isAfterOrEqual(validFrom) && date.isBeforeOrEqual(validTo))
				return tarmedLeistung;
		}
		return null;
	}
}
