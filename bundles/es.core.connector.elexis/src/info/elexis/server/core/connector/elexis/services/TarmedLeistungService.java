package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.billable.IBillable;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarTarmedLeistung;
import info.elexis.server.core.connector.elexis.billable.tarmed.TarmedExclusive;
import info.elexis.server.core.connector.elexis.billable.tarmed.TarmedKumulationType;
import info.elexis.server.core.connector.elexis.billable.tarmed.TarmedLimitation;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedDefinitionen;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedDefinitionen_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedExtension;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedGroup;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedGroup_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedKumulation;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedKumulation_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung.MandantType;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;

public class TarmedLeistungService extends PersistenceService {

	private static Logger log = LoggerFactory.getLogger(TarmedLeistungService.class);

	/**
	 * convenience method
	 * 
	 * @param id
	 * @return
	 */
	public static Optional<TarmedLeistung> load(String id) {
		return PersistenceService.load(TarmedLeistung.class, id).map(v -> (TarmedLeistung) v);
	}

	/**
	 * Convenience method
	 * 
	 * @param code
	 * @return
	 * @see #getVerrechenbarFromCode(String, TimeTool, String)
	 */
	@SuppressWarnings("rawtypes")
	public static Optional<IBillable> getVerrechenbarFromCode(String code) {
		return getVerrechenbarFromCode(code, null, null);
	}

	/**
	 * Query for a {@link TarmedLeistung} using the code. The returned
	 * {@link TarmedLeistung} will be valid on date, and will be from the cataloge
	 * specified by law.
	 * 
	 * @param code
	 * @param date
	 * @param law
	 * @return null if no matching {@link TarmedLeistung} found
	 */
	@SuppressWarnings("rawtypes")
	public static Optional<IBillable> getVerrechenbarFromCode(String code, TimeTool date, String law) {
		Optional<TarmedLeistung> tl = findFromCode(code, date, law);
		if (tl.isPresent()) {
			return Optional.of(new VerrechenbarTarmedLeistung(tl.get()));
		}

		log.error("TarmedLeistung " + code + " not found!");

		return Optional.empty();
	}

	/**
	 * Get the exclusions as String, containing the service and chapter codes. Group
	 * exclusions are NOT part of the String.
	 * 
	 * @param tl
	 * @param date
	 * @return
	 */
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
			if ("G".equals(excl.getSlaveArt())) {
				continue;
			}
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

	/**
	 * Convenience method
	 * 
	 * @param code
	 * @return
	 * @see #findFromCode(String, TimeTool, String)
	 */
	public static Optional<TarmedLeistung> findFromCode(String code) {
		return findFromCode(code, null);
	}

	/**
	 * Convenience method
	 * 
	 * @param code
	 * @param date
	 * @return
	 * @see #findFromCode(String, TimeTool, String)
	 */
	public static Optional<TarmedLeistung> findFromCode(String code, TimeTool date) {
		return findFromCode(code, date, null);
	}

	/**
	 * Query for a {@link TarmedLeistung} using the code. The returned
	 * {@link TarmedLeistung} will be valid on date, and will be from the cataloge
	 * specified by law.
	 * 
	 * @param code
	 * @param date
	 * @param law
	 * @return 
	 */
	public static Optional<TarmedLeistung> findFromCode(final String code, TimeTool date, String law) {
		if (date == null) {
			date = new TimeTool();
		}
		JPAQuery<TarmedLeistung> query = new JPAQuery<TarmedLeistung>(TarmedLeistung.class);
		query.add(TarmedLeistung_.code_, QUERY.EQUALS, code);
		if (law != null) {
			query.add(TarmedLeistung_.law, QUERY.EQUALS, law.toLowerCase());
		}
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

	/**
	 * 
	 * @param tarmedLeistung
	 * @return the cummulated minutes required to perform a {@link TarmedLeistung}.
	 */
	public static int getMinutesForTarmedLeistung(TarmedLeistung tarmedLeistung) {
		TarmedExtension extension = tarmedLeistung.getExtension();
		if (extension != null) {
			double min = 0d;
			min += ServiceUtil.checkZeroDouble(extension.getLimits().get("LSTGIMES_MIN"));
			min += ServiceUtil.checkZeroDouble(extension.getLimits().get("VBNB_MIN"));
			min += ServiceUtil.checkZeroDouble(extension.getLimits().get("BEFUND_MIN"));
			min += ServiceUtil.checkZeroDouble(extension.getLimits().get("WECHSEL_MIN"));
			return (int) Math.round(min);
		}
		return 0;
	}

	public static String MANDANT_TYPE_EXTINFO_KEY = "ch.elexis.data.tarmed.mandant.type";

	/**
	 * Get the {@link MandantType} of the {@link Mandant}. If not found the default
	 * value is {@link MandantType#SPECIALIST}.
	 * 
	 * @param mandant
	 * @return
	 */
	public static MandantType getMandantType(Kontakt mandator) {
		String typeObj = mandator.getExtInfoAsString(MANDANT_TYPE_EXTINFO_KEY);
		if (typeObj instanceof String) {
			return MandantType.valueOf((String) typeObj);
		}
		return MandantType.SPECIALIST;
	}

	/**
	 * Set the {@link MandantType} of the {@link Mandant}.
	 * 
	 * @param mandant
	 * @param type
	 */
	public static void setMandantType(Kontakt mandator, MandantType type) {
		mandator.setExtInfoValue(MANDANT_TYPE_EXTINFO_KEY, type.name());
	}

	/**
	 * Get the AL value of the {@link TarmedLeistung}. The {@link Mandant} is needed
	 * to determine special scaling factors. On billing of the
	 * {@link TarmedLeistung} the values for AL and TL should be set to the ExtInfo
	 * of the {@link Verrechnet} for later use.
	 * 
	 * @param mandator
	 * @return
	 */
	public static int getAL(TarmedLeistung tl, Kontakt mandator) {
		double scaling = 100;
		Map<String, String> ext = tl.getExtension().getLimits();
		if (mandator != null) {
			MandantType type = getMandantType(mandator);
			if (type == MandantType.PRACTITIONER) {
				double alScaling = ServiceUtil.checkZeroDouble(ext.get(TarmedLeistung.EXT_FLD_F_AL_R));
				if (scaling > 0.1) {
					scaling *= alScaling;
				}
			}
		}
		return (int) Math.round(ServiceUtil.checkZeroDouble(ext.get(TarmedLeistung.EXT_FLD_TP_AL)) * scaling);
	}

	public static Optional<TarmedGroup> findTarmedGroup(String groupName, String law, TimeTool validFrom) {
		JPAQuery<TarmedGroup> query = new JPAQuery<TarmedGroup>(TarmedGroup.class);
		query.add(TarmedGroup_.groupName, QUERY.EQUALS, groupName);
		query.add(TarmedGroup_.law, QUERY.EQUALS, law);
		List<TarmedGroup> groups = query.execute();
		groups = groups.stream().filter(g -> g.validAt(validFrom)).collect(Collectors.toList());
		if (!groups.isEmpty()) {
			return Optional.of(groups.get(0));
		}
		return Optional.empty();
	}

	/**
	 * Get the exclusions valid now as String, containing the service and chapter
	 * codes. Group exclusions are NOT part of the String.
	 * 
	 * @param kons
	 * 
	 * @return
	 */
	public static List<TarmedExclusion> getExclusions(TarmedLeistung tl, Behandlung kons) {
		TimeTool curTimeHelper;
		if (kons == null) {
			curTimeHelper = new TimeTool((new Date()));
		} else {
			curTimeHelper = new TimeTool(kons.getDatum());
		}
		return getExclusions(tl, curTimeHelper);
	}

	/**
	 * Get {@link TarmedExclusion} objects with this {@link TarmedLeistung} as
	 * master.
	 * 
	 * @param date
	 * @return
	 */
	public static List<TarmedExclusion> getExclusions(TarmedLeistung tl, TimeTool date) {
		return TarmedLeistungService.getExclusions(tl.getCode(),
				tl.isChapter() ? TarmedKumulationType.CHAPTER : TarmedKumulationType.SERVICE, date, tl.getLaw());
	}

	/**
	 * Get {@link TarmedExclusion} objects for all exclusions defined as
	 * {@link TarmedKumulation}, with code as master code and master type.
	 * 
	 * @param mastercode
	 * @param masterType
	 * @param date
	 * @param law
	 * @return
	 * @see ch.elexis.data.TarmedKumulation#getExclusions
	 */
	public static List<TarmedExclusion> getExclusions(String mastercode, TarmedKumulationType masterType, TimeTool date,
			String law) {
		JPAQuery<TarmedKumulation> query = new JPAQuery<>(TarmedKumulation.class);
		query.add(TarmedKumulation_.masterCode, QUERY.EQUALS, mastercode);
		query.add(TarmedKumulation_.masterArt, QUERY.EQUALS, masterType.getArt());

		if (law != null && !law.isEmpty()) {
			query.add(TarmedKumulation_.law, QUERY.EQUALS, law);
		}

		query.add(TarmedKumulation_.typ, QUERY.EQUALS, TarmedKumulation.TYP_EXCLUSION);

		List<TarmedKumulation> exclusions = query.execute();
		if (exclusions == null || exclusions.isEmpty()) {
			return Collections.emptyList();
		}
		exclusions = exclusions.stream().filter(k -> k.isValidKumulation(date)).collect(Collectors.toList());
		return exclusions.stream().map(k -> new TarmedExclusion(k)).collect(Collectors.toList());
	}

	public static List<TarmedExclusion> getExclusions(TarmedGroup tarmedGroup, Behandlung kons) {
		TimeTool date = (kons != null) ? new TimeTool(kons.getDatum()) : new TimeTool(new Date());
		return getExclusions(tarmedGroup.getCode(), TarmedKumulationType.GROUP, date, tarmedGroup.getLaw());
	}

	/**
	 * Get {@link TarmedExclusion} objects for all exclusions defined as
	 * {@link TarmedKumulation}, with code as master code and master type.
	 * 
	 * @param mastercode
	 * @param masterType
	 * @param date
	 * @param law
	 * @return
	 * @see ch.elexis.data.TarmedKumulation#getExclusives
	 */
	public static List<TarmedExclusive> getExclusives(String mastercode, TarmedKumulationType masterType, TimeTool date,
			String law) {
		JPAQuery<TarmedKumulation> query = new JPAQuery<TarmedKumulation>(TarmedKumulation.class);
		query.add(TarmedKumulation_.masterCode, QUERY.EQUALS, mastercode);
		query.add(TarmedKumulation_.masterArt, QUERY.EQUALS, masterType.getArt());
		if (law != null && !law.isEmpty()) {
			query.add(TarmedKumulation_.law, QUERY.EQUALS, law);
		}
		query.add(TarmedKumulation_.typ, QUERY.EQUALS, TarmedKumulation.TYP_EXCLUSIVE);

		List<TarmedKumulation> exclusives = query.execute();
		if (exclusives == null || exclusives.isEmpty()) {
			return Collections.emptyList();
		}
		exclusives = exclusives.stream().filter(k -> k.isValidKumulation(date)).collect(Collectors.toList());
		return exclusives.stream().map(k -> new TarmedExclusive(k)).collect(Collectors.toList());
	}

	public static List<TarmedLimitation> getLimitations(TarmedGroup group) {
		String lim = (String) group.getExtension().getLimits().get("limits"); //$NON-NLS-1$
		if (lim != null && !lim.isEmpty()) {
			List<TarmedLimitation> ret = new ArrayList<>();
			String[] lines = lim.split("#"); //$NON-NLS-1$
			for (String line : lines) {
				ret.add(TarmedLimitation.of(line).setTarmedGroup(group));
			}
			return ret;
		}
		return Collections.emptyList();
	}

	public static List<TarmedLimitation> getLimitations(TarmedLeistung tarmedLeistung) {
		String lim = (String) tarmedLeistung.getExtension().getLimits().get("limits"); //$NON-NLS-1$
		if (lim != null && !lim.isEmpty()) {
			List<TarmedLimitation> ret = new ArrayList<>();
			String[] lines = lim.split("#"); //$NON-NLS-1$
			for (String line : lines) {
				ret.add(TarmedLimitation.of(line).setTarmedLeistung(tarmedLeistung));
			}
			return ret;
		}
		return Collections.emptyList();
	}

	public static String getSparteAsText(TarmedLeistung tl) {
		JPAQuery<TarmedDefinitionen> query = new JPAQuery<>(TarmedDefinitionen.class);
		query.add(TarmedDefinitionen_.spalte, QUERY.EQUALS, "SPARTE");
		query.add(TarmedDefinitionen_.kuerzel, QUERY.EQUALS, tl.getSparte());
		List<TarmedDefinitionen> result = query.execute();
		if (!result.isEmpty()) {
			return result.get(0).getTitel();
		}
		return "";
	}

}
