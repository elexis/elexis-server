package info.elexis.server.core.connector.elexis.billable.tarmed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import ch.rgw.tools.Result;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.billable.IBillable;
import info.elexis.server.core.connector.elexis.billable.optifier.Messages;
import info.elexis.server.core.connector.elexis.billable.optifier.TarmedOptifier;
import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedGroup;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
import info.elexis.server.core.connector.elexis.services.UserconfigService;
import info.elexis.server.core.connector.elexis.services.VerrechnetService;

public class TarmedLimitation {

	private int amount;

	private String per;
	private String operator;

	private LimitationUnit limitationUnit;
	private int limitationAmount;

	private int electronicBilling;

	private boolean skip = false;

	private TarmedLeistung tarmedLeistung;
	private TarmedGroup tarmedGroup;

	public enum LimitationUnit {
		LOCATION_SESSION, SIDE, SESSION, PATIENT_SESSION, COVERAGE, STAY, TESTSERIES, PREGNANCY, BIRTH, RADIANTEXPOSURE, TRANSMITTAL, AUTOPSY, EXPERTISE, INTERVENTION_SESSION, CATEGORY_DAY, DAY, WEEK, MONTH, YEAR, JOINTREGION, REGION_SIDE, JOINTREGION_SIDE, MAINSERVICE, SESSION_YEAR, SESSION_COVERAGE, SESSION_PATIENT;

		public static LimitationUnit from(int parseInt) {
			switch (parseInt) {
			case 6:
				return LOCATION_SESSION;
			case 7:
				return SESSION;
			case 8:
				return COVERAGE;
			case 9:
				return PATIENT_SESSION;
			case 10:
				return SIDE;
			case 11:
				return STAY;
			case 12:
				return TESTSERIES;
			case 13:
				return PREGNANCY;
			case 14:
				return BIRTH;
			case 15:
			case 31:
				return RADIANTEXPOSURE;
			case 16:
				return TRANSMITTAL;
			case 17:
				return AUTOPSY;
			case 18:
				return EXPERTISE;
			case 19:
				return INTERVENTION_SESSION;
			case 20:
				return CATEGORY_DAY;
			case 21:
				return DAY;
			case 22:
				return WEEK;
			case 23:
				return MONTH;
			case 26:
				return YEAR;
			case 40:
				return JOINTREGION;
			case 41:
				return REGION_SIDE;
			case 42:
				return JOINTREGION_SIDE;
			case 45:
				return MAINSERVICE;
			case 51:
				return SESSION_YEAR;
			case 52:
				return SESSION_COVERAGE;
			case 53:
			case 54:
				return SESSION_PATIENT;
			}
			return null;
		}

	}

	/**
	 * Factory method for creating {@link TarmedLimitation} objects of
	 * {@link TarmedLeistung} limitations.
	 * 
	 * @param limitation
	 * @return
	 */
	public static TarmedLimitation of(String limitation) {
		TarmedLimitation ret = new TarmedLimitation();

		String[] parts = limitation.split(","); //$NON-NLS-1$
		if (parts.length >= 5) {
			if (parts[0] != null && !parts[0].isEmpty()) {
				ret.operator = parts[0].trim();
			}
			if (parts[1] != null && !parts[1].isEmpty()) {
				ret.amount = Float.valueOf(parts[1].trim()).intValue();
			}
			if (parts[2] != null && !parts[2].isEmpty()) {
				ret.limitationAmount = Float.valueOf(parts[2].trim()).intValue();
			}
			if (parts[3] != null && !parts[3].isEmpty()) {
				ret.per = parts[3].trim();
			}
			if (parts[4] != null && !parts[4].isEmpty()) {
				ret.limitationUnit = LimitationUnit.from(Float.valueOf(parts[4].trim()).intValue());
			}
		}
		if (parts.length >= 6) {
			if (parts[5] != null && !parts[5].isEmpty()) {
				ret.electronicBilling = Float.valueOf(parts[5].trim()).intValue();
			}
		} else {
			ret.electronicBilling = 0;
		}
		return ret;
	}

	public TarmedLimitation setTarmedLeistung(TarmedLeistung tarmedLeistung) {
		this.tarmedLeistung = tarmedLeistung;
		return this;
	}

	public TarmedLimitation setTarmedGroup(TarmedGroup tarmedGroup) {
		this.tarmedGroup = tarmedGroup;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (limitationUnit == LimitationUnit.SESSION) {
			sb.append(Messages.TarmedOptifier_codemax + amount + Messages.TarmedOptifier_perSession);
		} else if (limitationUnit == LimitationUnit.SIDE) {
			sb.append(Messages.TarmedOptifier_codemax + amount + Messages.TarmedOptifier_perSide);
		} else if (limitationUnit == LimitationUnit.DAY) {
			sb.append(Messages.TarmedOptifier_codemax + amount + Messages.TarmedOptifier_perDay);
		} else if (limitationUnit == LimitationUnit.WEEK) {
			if (tarmedGroup != null) {
				sb.append(String.format(Messages.TarmedOptifier_groupmax, tarmedGroup.getCode()) + amount
						+ String.format(Messages.TarmedOptifier_perWeeks, limitationAmount));
			} else {
				sb.append(Messages.TarmedOptifier_codemax + amount
						+ String.format(Messages.TarmedOptifier_perWeeks, limitationAmount));
			}
		} else if (limitationUnit == LimitationUnit.MONTH) {
			if (tarmedGroup != null) {
				sb.append(String.format(Messages.TarmedOptifier_groupmax, tarmedGroup.getCode()) + amount
						+ String.format(Messages.TarmedOptifier_perMonth, limitationAmount));
			} else {
				sb.append(Messages.TarmedOptifier_codemax + amount
						+ String.format(Messages.TarmedOptifier_perMonth, limitationAmount));
			}
		} else if (limitationUnit == LimitationUnit.YEAR) {
			if (tarmedGroup != null) {
				sb.append(String.format(Messages.TarmedOptifier_groupmax, tarmedGroup.getCode()) + amount
						+ String.format(Messages.TarmedOptifier_perYears, limitationAmount));
			} else {
				sb.append(Messages.TarmedOptifier_codemax + amount
						+ String.format(Messages.TarmedOptifier_perYears, limitationAmount));
			}
		} else if (limitationUnit == LimitationUnit.COVERAGE) {
			sb.append(Messages.TarmedOptifier_codemax + amount + Messages.TarmedOptifier_perCoverage);
		} else {
			sb.append("amount " + amount + "x unit " + limitationAmount + "x" + limitationUnit);
		}
		return sb.toString();
	}

	public boolean isTestable() {
		return limitationUnit == LimitationUnit.SIDE || limitationUnit == LimitationUnit.SESSION
				|| limitationUnit == LimitationUnit.DAY || limitationUnit == LimitationUnit.WEEK
				|| limitationUnit == LimitationUnit.MONTH || limitationUnit == LimitationUnit.YEAR
				|| limitationUnit == LimitationUnit.COVERAGE;
	}

	@SuppressWarnings("rawtypes")
	public Result<IBillable> test(Behandlung kons, Verrechnet newVerrechnet) {
		if (limitationUnit == LimitationUnit.SIDE || limitationUnit == LimitationUnit.SESSION) {
			return testSideOrSession(kons, newVerrechnet);
		} else if (limitationUnit == LimitationUnit.DAY) {
			return testDay(kons, newVerrechnet);
		} else if (limitationUnit == LimitationUnit.WEEK || limitationUnit == LimitationUnit.MONTH
				|| limitationUnit == LimitationUnit.YEAR) {
			return testDuration(kons, newVerrechnet);
		} else if (limitationUnit == LimitationUnit.COVERAGE) {
			return testCoverage(kons, newVerrechnet);
		}
		return new Result<IBillable>(null);
	}

	@SuppressWarnings("rawtypes")
	private Result<IBillable> testCoverage(Behandlung kons, Verrechnet verrechnet) {
		Result<IBillable> ret = new Result<IBillable>(null);
		if (shouldSkipTest(kons.getMandant())) {
			return ret;
		}
		if (operator.equals("<=")) {
			if (tarmedGroup == null) {
				List<Verrechnet> verrechnetByCoverage = getVerrechnetByCoverageAndCode(kons, tarmedLeistung.getCode());
				verrechnetByCoverage = filterWithSameCode(verrechnet, verrechnetByCoverage);
				if (getVerrechnetCount(verrechnetByCoverage) > amount) {
					ret = new Result<IBillable>(Result.SEVERITY.WARNING, TarmedOptifier.KUMULATION, toString(), null,
							false);
				}
			} else {
				List<Verrechnet> allVerrechnetOfGroup = new ArrayList<>();
				List<String> serviceCodes = tarmedGroup.getServices();
				for (String code : serviceCodes) {
					allVerrechnetOfGroup.addAll(getVerrechnetByCoverageAndCode(kons, code));
				}
				if (getVerrechnetCount(allVerrechnetOfGroup) > amount) {
					ret = new Result<IBillable>(Result.SEVERITY.WARNING, TarmedOptifier.KUMULATION, toString(), null,
							false);
				}
			}
		}
		return ret;
	}

	@SuppressWarnings("rawtypes")
	private Result<IBillable> testDuration(Behandlung kons, Verrechnet verrechnet) {
		Result<IBillable> ret = new Result<IBillable>(null);
		if (shouldSkipTest(kons.getMandant())) {
			return ret;
		}
		if (operator.equals("<=")) {
			if (tarmedGroup == null) {
				List<Verrechnet> verrechnetByMandant = getVerrechnetByMandantAndCodeDuring(kons,
						VerrechnetService.getVerrechenbar(verrechnet).get().getCode());
				if (getVerrechnetCount(verrechnetByMandant) > amount) {
					ret = new Result<IBillable>(Result.SEVERITY.WARNING, TarmedOptifier.KUMULATION, toString(), null,
							false);
				}
			} else {
				List<Verrechnet> allVerrechnetOfGroup = new ArrayList<>();
				List<String> serviceCodes = tarmedGroup.getServices();
				for (String code : serviceCodes) {
					allVerrechnetOfGroup.addAll(getVerrechnetByMandantAndCodeDuring(kons, code));
				}
				if (getVerrechnetCount(allVerrechnetOfGroup) > amount) {
					ret = new Result<IBillable>(Result.SEVERITY.WARNING, TarmedOptifier.KUMULATION, toString(), null,
							false);
				}
			}
		}
		return ret;
	}

	private int getVerrechnetCount(List<Verrechnet> verrechnete) {
		int ret = 0;
		for (Verrechnet verrechnet : verrechnete) {
			ret += verrechnet.getZahl();
		}
		return ret;
	}

	// @formatter:off
	private static final String VERRECHNET_BYMANDANT_ANDCODE_DURING = "SELECT leistungen.* FROM leistungen, behandlungen, faelle"
			+ " WHERE leistungen.deleted = '0'" + " AND leistungen.deleted = behandlungen.deleted"
			+ " AND leistungen.BEHANDLUNG = behandlungen.ID"
			+ " AND leistungen.KLASSE = 'ch.elexis.data.TarmedLeistung'" + " AND faelle.ID = behandlungen.fallID"
			+ " AND faelle.PatientID = ?" + " AND leistungen.LEISTG_CODE like ?" + " AND behandlungen.Datum >= ?"
			+ " AND behandlungen.MandantID = ?";
	// @formatter:on

	private List<Verrechnet> getVerrechnetByMandantAndCodeDuring(Behandlung kons, String code) {
		LocalDate fromDate = getDuringStartDate(kons);
		Kontakt mandant = kons.getMandant();
		if (fromDate != null && mandant != null) {
			EntityManager em = ElexisEntityManager.createEntityManager();
			try {
				Query pstm = em.createNativeQuery(VERRECHNET_BYMANDANT_ANDCODE_DURING, Verrechnet.class);
				pstm.setParameter(1, kons.getFall().getPatient().getId());
				pstm.setParameter(2, code + "%");
				pstm.setParameter(3, fromDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
				pstm.setParameter(4, mandant.getId());
				return pstm.getResultList();
			} finally {
				em.close();
			}
		}
		return Collections.emptyList();
	}

	// @formatter:off
	private static final String VERRECHNET_BYCOVERAGE_ANDCODE = "SELECT leistungen.* FROM leistungen, behandlungen"
			+ " WHERE leistungen.deleted = '0'" + " AND leistungen.deleted = behandlungen.deleted"
			+ " AND leistungen.BEHANDLUNG = behandlungen.ID"
			+ " AND leistungen.KLASSE = 'ch.elexis.data.TarmedLeistung'" + " AND leistungen.LEISTG_CODE like ?"
			+ " AND behandlungen.FallID = ?";
	// @formatter:on

	private List<Verrechnet> getVerrechnetByCoverageAndCode(Behandlung kons, String code) {
		if (kons != null && kons.getFall() != null) {
			EntityManager em = ElexisEntityManager.createEntityManager();
			try {
				Query pstm = em.createNativeQuery(VERRECHNET_BYCOVERAGE_ANDCODE, Verrechnet.class);
				pstm.setParameter(1, code + "%");
				pstm.setParameter(2, kons.getFall().getId());
				return pstm.getResultList();
			} finally {
				em.close();
			}
		}
		return Collections.emptyList();
	}

	private LocalDate getDuringStartDate(Behandlung kons) {
		LocalDate konsDate = new TimeTool(kons.getDatum()).toLocalDate();
		LocalDate ret = null;
		if (limitationUnit == LimitationUnit.WEEK) {
			ret = konsDate.minus(limitationAmount, ChronoUnit.WEEKS);
		} else if (limitationUnit == LimitationUnit.MONTH) {
			ret = konsDate.minus(limitationAmount, ChronoUnit.MONTHS);
		} else if (limitationUnit == LimitationUnit.YEAR) {
			ret = konsDate.minus(limitationAmount, ChronoUnit.YEARS);
		}
		if (tarmedLeistung != null && ret != null) {
			LocalDate leistungDate = tarmedLeistung.getGueltigVon();
			if (ret.isBefore(leistungDate)) {
				ret = leistungDate;
			}
		}
		return ret;
	}
	
	/**
	 * Filter the list of {@link Verrechnet} that only instances with the same code field (Tarmed
	 * code, startdate and law) as the provided {@link Verrechnet} are in the resulting list.
	 * 
	 * @param verrechnet
	 * @return
	 */
	private List<Verrechnet> filterWithSameCode(Verrechnet verrechnet, List<Verrechnet> list){
		List<Verrechnet> ret = new ArrayList<>();
		String matchCode = verrechnet.getLeistungenCode();
		if(matchCode != null && !matchCode.isEmpty()) {
			for (Verrechnet element : list) {
				if (matchCode.equals(element.getLeistungenCode())) {
					ret.add(element);
				}
			}
		}
		return ret;
	}

	@SuppressWarnings("rawtypes")
	private Result<IBillable> testDay(Behandlung kons, Verrechnet verrechnet) {
		Result<IBillable> ret = new Result<IBillable>(null);
		if (shouldSkipTest(kons.getMandant())) {
			return ret;
		}
		if (limitationAmount == 1 && operator.equals("<=")) {
			if (verrechnet.getZahl() > amount) {
				ret = new Result<IBillable>(Result.SEVERITY.WARNING, TarmedOptifier.KUMULATION, toString(), null,
						false);
			}
		}
		return ret;
	}

	@SuppressWarnings("rawtypes")
	private Result<IBillable> testSideOrSession(Behandlung kons, Verrechnet verrechnet) {
		Result<IBillable> ret = new Result<IBillable>(null);
		if (shouldSkipTest(kons.getMandant())) {
			return ret;
		}
		if (limitationAmount == 1 && operator.equals("<=")) {
			if (verrechnet.getZahl() > amount) {
				if (limitationUnit == LimitationUnit.SESSION) {
					ret = new Result<IBillable>(Result.SEVERITY.WARNING, TarmedOptifier.KUMULATION, toString(), null,
							false);
				} else if (limitationUnit == LimitationUnit.SIDE) {
					ret = new Result<IBillable>(Result.SEVERITY.WARNING, TarmedOptifier.KUMULATION, toString(), null,
							false);
				}
			}
		}
		return ret;
	}

	private boolean shouldSkipTest(Kontakt mandatorContact) {
		if (skip) {
			return skip;
		}
		return shouldSkipElectronicBilling(mandatorContact);
	}

	private boolean shouldSkipElectronicBilling(Kontakt mandatorContact) {
		if (electronicBilling > 0) {
			boolean result = UserconfigService.get(mandatorContact, TarmedOptifier.BILL_ELECTRONICALLY, false);
			if(result) {
				return true;
			}
		}
		return false;
	}

	public LimitationUnit getLimitationUnit() {
		return limitationUnit;
	}

	public int getAmount() {
		return amount;
	}

	public void setSkip(boolean value) {
		this.skip = true;
	}
}
