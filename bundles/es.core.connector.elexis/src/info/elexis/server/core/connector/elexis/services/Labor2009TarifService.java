//package info.elexis.server.core.connector.elexis.services;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//
//import ch.rgw.tools.TimeTool;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Labor2009Tarif;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Labor2009Tarif_;
//
//public class Labor2009TarifService extends PersistenceService {
//
//	/**
//	 * convenience method
//	 * 
//	 * @param id
//	 * @return
//	 */
//	public static Optional<Labor2009Tarif> load(String id) {
//		return PersistenceService.load(Labor2009Tarif.class, id).map(v -> (Labor2009Tarif) v);
//	}
//
//	public static Optional<Labor2009Tarif> findFromCode(String code) {
//		return findFromCode(code, null);
//	}
//
//	public static Optional<Labor2009Tarif> findFromCode(String code, TimeTool date) {
//		if (date == null) {
//			date = new TimeTool();
//		}
//		JPAQuery<Labor2009Tarif> query = new JPAQuery<Labor2009Tarif>(Labor2009Tarif.class);
//		query.add(Labor2009Tarif_.code, JPAQuery.QUERY.LIKE, code);
//		List<Labor2009Tarif> leistungen = query.execute();
//		for (Labor2009Tarif laborLeistung : leistungen) {
//			TimeTool validFrom = new TimeTool(laborLeistung.getGueltigVon());
//			LocalDate validToL = laborLeistung.getGueltigBis();
//			TimeTool validTo = new TimeTool((validToL != null) ? validToL : LocalDate.of(2999, 12, 31));
//			if (date.isAfterOrEqual(validFrom) && date.isBeforeOrEqual(validTo))
//				return Optional.of(laborLeistung);
//		}
//		return Optional.empty();
//	}
//
//}
