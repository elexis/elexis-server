package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.PhysioLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.PhysioLeistung_;

public class PhysioLeistungService extends AbstractService<PhysioLeistung> {
	public static PhysioLeistungService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final PhysioLeistungService INSTANCE = new PhysioLeistungService();
	}

	private PhysioLeistungService() {
		super(PhysioLeistung.class);
	}

	public static Optional<PhysioLeistung> findFromCode(String code) {
		return findFromCode(code, null);
	}

	public static Optional<PhysioLeistung> findFromCode(String code, TimeTool date) {
		if (date == null) {
			date = new TimeTool();
		}
		JPAQuery<PhysioLeistung> query = new JPAQuery<PhysioLeistung>(PhysioLeistung.class);
		query.add(PhysioLeistung_.ziffer, JPAQuery.QUERY.LIKE, code);
		List<PhysioLeistung> leistungen = query.execute();
		for (PhysioLeistung physioLeistung : leistungen) {
			TimeTool validFrom = new TimeTool(physioLeistung.getValidFrom());
			LocalDate validToL = physioLeistung.getValidUntil();
			TimeTool validTo = new TimeTool((validToL != null) ? validToL : LocalDate.of(2999, 12, 31));
			if (date.isAfterOrEqual(validFrom) && date.isBeforeOrEqual(validTo))
				return Optional.of(physioLeistung);
		}
		return Optional.empty();
	}
}
