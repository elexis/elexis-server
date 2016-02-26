package info.elexis.server.core.connector.elexis.billable.optifier;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ch.elexis.core.constants.Preferences;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.common.ObjectStatus;
import info.elexis.server.core.connector.elexis.billable.IVerrechenbar;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarTarmedLeistung;
import info.elexis.server.core.connector.elexis.internal.BundleConstants;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
import info.elexis.server.core.connector.elexis.services.FallService;
import info.elexis.server.core.connector.elexis.services.KontaktService;
import info.elexis.server.core.connector.elexis.services.TarmedLeistungService;
import info.elexis.server.core.connector.elexis.services.UserconfigService;
import info.elexis.server.core.connector.elexis.services.VerrechnetService;

/**
 * port of TarmedOptifier from Elexis RCP
 */
public class TarmedOptifier implements IOptifier {
	private static final String TL = "TL"; //$NON-NLS-1$
	private static final String AL = "AL"; //$NON-NLS-1$
	public static final int OK = 0;
	public static final int PREISAENDERUNG = 1;
	public static final int KUMULATION = 2;
	public static final int KOMBINATION = 3;
	public static final int EXKLUSION = 4;
	public static final int INKLUSION = 5;
	public static final int LEISTUNGSTYP = 6;
	public static final int NOTYETVALID = 7;
	public static final int NOMOREVALID = 8;

	public static final String SIDE = "Seite";
	public static final String SIDE_L = "l";
	public static final String SIDE_R = "r";
	public static final String LEFT = "left";
	public static final String RIGHT = "right";

	private static final String CHAPTER_XRAY = "39.02";
	private static final String DEFAULT_TAX_XRAY_ROOM = "39.2000";

	public static final String PREF_ADDCHILDREN = "tarmed/addchildrentp";
	public static final String BILL_ELECTRONICALLY = "TarmedBillElectronic";

	boolean bOptify = true;
	private Verrechnet newVerrechnet;

	/**
	 * Eine Verrechnungsposition zufügen. Der Optifier muss prüfen, ob die
	 * Verrechnungsposition im Kontext der übergebenen Konsultation verwendet
	 * werden kann und kann sie ggf. zurückweisen oder modifizieren.
	 */

	public IStatus add(IVerrechenbar code, Behandlung kons, String userId, String mandatorId) {

		bOptify = UserconfigService.INSTANCE.get(userId, Preferences.LEISTUNGSCODES_OPTIFY, true);

		TarmedLeistung tc = (TarmedLeistung) code;
		List<Verrechnet> lst = kons.getVerrechnet();
		boolean checkBezug = false;
		boolean bezugOK = true;
		/*
		 * TODO Hier checken, ob dieser code mit der Dignität und
		 * Fachspezialisierung des aktuellen Mandanten usw. vereinbar ist
		 */

		Map<Object, Object> ext = tc.getExtension().getLimits();

		// Bezug prüfen
		String bezug = (String) ext.get("Bezug"); //$NON-NLS-1$
		if (!StringTool.isNothing(bezug)) {
			checkBezug = true;
			bezugOK = false;
		}
		// Gültigkeit gemäss Datum prüfen
		if (bOptify) {
			LocalDate date = kons.getDatum();
			if (!StringTool.isNothing(tc.getGueltigVon())) {
				LocalDate tVon = tc.getGueltigVon();
				if (date.isBefore(tVon)) {
					return new Status(Status.WARNING, BundleConstants.BUNDLE_ID,
							code.getCode() + Messages.TarmedOptifier_NotYetValid);
				}
			}
			if (!StringTool.isNothing(tc.getGueltigBis())) {
				LocalDate tBis = tc.getGueltigBis();
				if (date.isAfter(tBis)) {
					return new Status(Status.WARNING, BundleConstants.BUNDLE_ID,
							code.getCode() + Messages.TarmedOptifier_NoMoreValid);
				}
			}
		}
		newVerrechnet = null;
		// Korrekter Fall Typ prüfen, und ggf. den code ändern
		if (tc.getCode().matches("39.002[01]") || tc.getCode().matches("39.001[0156]")) {
			String gesetz = FallService.getRequiredString(kons.getFall(), "Gesetz");
			if (gesetz == null || gesetz.isEmpty()) {
				gesetz = FallService.getAbrechnungsSystem(kons.getFall());
			}

			if ("KVG".equalsIgnoreCase(gesetz) && tc.getCode().matches("39.0011")) {
				return add(TarmedLeistungService.INSTANCE.getVerrechenbarFromCode("39.0010"), kons, userId, mandatorId);
			} else if (!gesetz.equalsIgnoreCase("KVG") && tc.getCode().matches("39.0010")) {
				return add(TarmedLeistungService.INSTANCE.getVerrechenbarFromCode("39.0011"), kons, userId, mandatorId);
			}

			if ("KVG".equalsIgnoreCase(gesetz) && tc.getCode().matches("39.0016")) {
				return add(TarmedLeistungService.INSTANCE.getVerrechenbarFromCode("39.0015"), kons, userId, mandatorId);
			} else if (!gesetz.equalsIgnoreCase("KVG") && tc.getCode().matches("39.0015")) {
				return add(TarmedLeistungService.INSTANCE.getVerrechenbarFromCode("39.0016"), kons, userId, mandatorId);
			}

			if ("KVG".equalsIgnoreCase(gesetz) && tc.getCode().matches("39.0021")) {
				return this.add(TarmedLeistungService.INSTANCE.getVerrechenbarFromCode("39.0020"), kons, userId,
						mandatorId);
			} else if (!gesetz.equalsIgnoreCase("KVG") && tc.getCode().matches("39.0020")) {
				return this.add(TarmedLeistungService.INSTANCE.getVerrechenbarFromCode("39.0021"), kons, userId,
						mandatorId);
			}
		}

		// Ist der Hinzuzufügende Code vielleicht schon in der Liste? Dann
		// nur Zahl erhöhen.
		for (Verrechnet v : lst) {
			String side = getSide(v);
			if (isInstance(v, code) && (side.equals("none") || (tc.requiresSide() && side.equals(LEFT)))) {
				newVerrechnet = v;
				newVerrechnet.setZahl(newVerrechnet.getZahl() + 1);
				if (bezugOK) {
					break;
				}
			}
			// "Nur zusammen mit" - Bedingung erfüllt ?
			if (checkBezug && bOptify) {
				Optional<IVerrechenbar> ver = VerrechnetService.INSTANCE.getVerrechenbar(v);
				if (ver.isPresent() && ver.get().getCode().equals(bezug)) {
					bezugOK = true;
					if (newVerrechnet != null) {
						break;
					}
				}
			}
		}
		// Ausschliessende Kriterien prüfen ("Nicht zusammen mit")
		if (newVerrechnet == null) {
			newVerrechnet = VerrechnetService.INSTANCE.create(code, kons, 1);
			// Exclusionen
			if (bOptify) {
				TarmedLeistung newTarmed = (TarmedLeistung) code;
				for (Verrechnet v : lst) {
					Optional<IVerrechenbar> verrechenbar = VerrechnetService.INSTANCE.getVerrechenbar(v);
					if (verrechenbar.isPresent() && verrechenbar.get() instanceof TarmedLeistung) {
						TarmedLeistung tarmed = (TarmedLeistung) verrechenbar.get();
						// check if new has an exclusion for this verrechnet
						// tarmed
						IStatus resCompatible = isCompatible(tarmed, newTarmed);
						if (resCompatible.isOK()) {
							// check if existing tarmed has exclusion for
							// new one
							resCompatible = isCompatible(newTarmed, tarmed);
						}

						if (!resCompatible.isOK()) {
							VerrechnetService.INSTANCE.delete(newVerrechnet);
							return resCompatible;
						}
					}
				}

				Optional<IVerrechenbar> verrechenbar = VerrechnetService.INSTANCE.getVerrechenbar(newVerrechnet);
				if (verrechenbar.isPresent()) {
					if (verrechenbar.get().getCode().equals("00.0750")
							|| verrechenbar.get().getCode().equals("00.0010")) {
						String excludeCode = null;
						if (verrechenbar.get().getCode().equals("00.0010")) {
							excludeCode = "00.0750";
						} else {
							excludeCode = "00.0010";
						}
						for (Verrechnet v : lst) {
							Optional<IVerrechenbar> vr = VerrechnetService.INSTANCE.getVerrechenbar(v);
							if (vr.isPresent() && vr.get().getCode().equals(excludeCode)) {
								VerrechnetService.INSTANCE.delete(newVerrechnet);
								return new Status(Status.WARNING, BundleConstants.BUNDLE_ID,
										"00.0750 ist nicht im Rahmen einer ärztlichen Beratung 00.0010 verrechnenbar.");
							}
						}
					}
				}

			}
			newVerrechnet.setDetail(AL, Integer.toString(tc.getAL()));
			newVerrechnet.setDetail(TL, Integer.toString(tc.getTL()));
			lst.add(newVerrechnet);
		}

		// check if side is required
		if (tc.requiresSide()) {
			newVerrechnet.setDetail(SIDE, SIDE_L);
		}

		/*
		 * Dies führt zu Fehlern bei Codes mit mehreren Master-Möglichkeiten ->
		 * vorerst raus // "Zusammen mit" - Bedingung nicht erfüllt ->
		 * Hauptziffer einfügen. if(checkBezug){ if(bezugOK==false){
		 * TarmedLeistung tl=TarmedLeistung.load(bezug); Result<IVerrechenbar>
		 * r1=add(tl,kons); if(!r1.isOK()){
		 * r1.add(Log.WARNINGS,KOMBINATION,code.getCode()+" nur zusammen mit
		 * "+bezug,null,false); //$NON-NLS-1$ return r1; } } }
		 */

		// Prüfen, ob zu oft verrechnet - diese Version prüft nur "pro
		// Sitzung" und "pro Tag".
		if (bOptify) {
			String lim = (String) ext.get("limits"); //$NON-NLS-1$
			if (lim != null) {
				String[] lin = lim.split("#"); //$NON-NLS-1$
				for (String line : lin) {
					String[] f = line.split(","); //$NON-NLS-1$
					if (f.length == 5) {
						switch (Integer.parseInt(f[4].trim())) {
						case 7: // Pro Sitzung
							Optional<IVerrechenbar> verrechenbar = VerrechnetService.INSTANCE
									.getVerrechenbar(newVerrechnet);
							if (verrechenbar.isPresent() && verrechenbar.get().getCode().equals("00.0020")) {
								boolean result = UserconfigService.INSTANCE.get(mandatorId, BILL_ELECTRONICALLY, false);
								if (result) {
									break;
								}
							}
							// todo check if electronic billing
							if (f[2].equals("1") && f[0].equals("<=")) { // 1 //$NON-NLS-1$
																			// Sitzung
								int menge = Math.round(Float.parseFloat(f[1]));
								if (newVerrechnet.getZahl() > menge) {
									newVerrechnet.setZahl(menge);
									return new Status(Status.WARNING, BundleConstants.BUNDLE_ID,
											Messages.TarmedOptifier_codemax + menge
													+ Messages.TarmedOptifier_perSession);
								}
							}
							break;
						case 21: // Pro Tag
							if (f[2].equals("1") && f[0].equals("<=")) { // 1
																			// Tag
								int menge = Math.round(Float.parseFloat(f[1]));
								if (newVerrechnet.getZahl() > menge) {
									newVerrechnet.setZahl(menge);
									return new Status(Status.WARNING, BundleConstants.BUNDLE_ID,
											Messages.TarmedOptifier_codemax + menge + "Mal pro Tag");
								}
							}

							break;
						default:
							break;
						}
					}
				}
			}
		}

		String tcid = code.getCode();

		// check if it's an X-RAY service and add default tax if so
		// default xray tax will only be added once (see above)
		if (tc.getParent().startsWith(CHAPTER_XRAY)) {
			add(TarmedLeistungService.INSTANCE.getVerrechenbarFromCode(DEFAULT_TAX_XRAY_ROOM), kons, userId,
					mandatorId);
		}

		// double factor =
		// PersistentObject.checkZeroDouble(check.get("VK_Scale"));
		// Abzug für Praxis-Op. (alle TL von OP I auf 40% reduzieren)
		if ("35.0020".equals(tcid)) {

			double sum = 0.0;
			for (Verrechnet v : lst) {
				Optional<IVerrechenbar> verrechenbar = VerrechnetService.INSTANCE.getVerrechenbar(v);
				if (verrechenbar.isPresent() && verrechenbar.get() instanceof TarmedLeistung) {
					TarmedLeistung tl = (TarmedLeistung) verrechenbar.get();
					if ("OP I".equals(tl.getSparte())) {
						/*
						 * int tech = tl.getTL(); double abzug = tech 4.0 /
						 * 10.0; sum -= abzug;
						 */
						sum += tl.getTL();
					}
				}
			}

			// check.setPreis(new Money(sum));
			newVerrechnet.setTP(sum);
			newVerrechnet.setDetail(TL, Double.toString(sum));
			newVerrechnet.setPrimaryScaleFactor(-0.4);
			/*
			 * double sum=0.0; for(Verrechnet v:lst){ if(v.getVerrechenbar()
			 * instanceof TarmedLeistung){ TarmedLeistung tl=(TarmedLeistung)
			 * v.getVerrechenbar(); if(tl.getSparteAsText().equals("OP I")){ int
			 * tech=tl.getTL(); sum+=tech; } } } double scale=-0.4;
			 * check.setDetail("scale", Double.toString(scale));
			 * sum=sumfactor/100.0; check.setPreis(new Money(sum));
			 */
		}

		// Interventionelle Schmerztherapie: Zuschlag cervical und thoracal
		else if ("29.2090".equals(tcid)) {
			double sumAL = 0.0;
			double sumTL = 0.0;
			for (Verrechnet v : lst) {
				Optional<IVerrechenbar> verrechenbar = VerrechnetService.INSTANCE.getVerrechenbar(v);
				if (verrechenbar.isPresent() && verrechenbar.get() instanceof TarmedLeistung) {
					TarmedLeistung tl = (TarmedLeistung) verrechenbar.get();
					String tlc = tl.getCode();
					double z = v.getZahl();
					if (tlc.matches("29.20[12345678]0") || (tlc.equals("29.2200"))) {
						sumAL += (z * tl.getAL()) / 2;
						sumTL += (z * tl.getTL()) / 4;
					}
				}
			}
			newVerrechnet.setTP(sumAL + sumTL);
			newVerrechnet.setDetail(AL, Double.toString(sumAL));
			newVerrechnet.setDetail(TL, Double.toString(sumTL));
		}

		// Zuschlag Kinder
		else if ("00.0010".equals(tcid) || "00.0060".equals(tcid)) {
			boolean result = UserconfigService.INSTANCE.get(mandatorId, PREF_ADDCHILDREN, false);

			if (result) {
				Fall f = kons.getFall();
				if (f != null) {
					Kontakt p = f.getPatientKontakt();
					if (p != null) {
						int alter = KontaktService.getAgeInYears(p);
						if (alter >= 0 && alter < 6) {
							TarmedLeistung tl = TarmedLeistungService.getFromCode("00.0040",
									new TimeTool(kons.getDatum()));
							add(new VerrechenbarTarmedLeistung(tl), kons, userId, mandatorId);
						}
					}
				}
			}
		}

		// Zuschläge für Insellappen 50% auf AL und TL bei 1910,20,40,50
		else if ("04.1930".equals(tcid)) {
			double sumAL = 0.0;
			double sumTL = 0.0;
			for (Verrechnet v : lst) {
				Optional<IVerrechenbar> verrechenbar = VerrechnetService.INSTANCE.getVerrechenbar(v);
				if (verrechenbar.isPresent() && verrechenbar.get() instanceof TarmedLeistung) {
					TarmedLeistung tl = (TarmedLeistung) verrechenbar.get();
					String tlc = tl.getCode();
					int z = v.getZahl();
					if ("04.1910".equals(tlc) || "04.1920".equals(tlc) || "04.1940".equals(tlc)
							|| "04.1950".equals(tlc)) {
						sumAL += tl.getAL() * z;
						sumTL += tl.getTL() * z;
						// double al = (tl.getAL() * 15) / 10.0;
						// double tel = (tl.getTL() * 15) / 10.0;
						// sum += al * z;
						// sum += tel * z;
					}
				}
			}
			// sum = sum * factor / 100.0;
			// check.setPreis(new Money(sum));
			newVerrechnet.setTP(sumAL + sumTL);
			newVerrechnet.setDetail(AL, Double.toString(sumAL));
			newVerrechnet.setDetail(TL, Double.toString(sumTL));
			newVerrechnet.setPrimaryScaleFactor(0.5);
		}
		// Notfall-Zuschläge
		if (tcid.startsWith("00.25")) { //$NON-NLS-1$
			double sum = 0.0;
			int subcode = Integer.parseInt(tcid.substring(5));
			switch (subcode) {
			case 10: // Mo-Fr 7-19, Sa 7-12: 60 TP
				break;
			case 20: // Mo-Fr 19-22, Sa 12-22, So 7-22: 120 TP
				break;
			case 30: // 25% zu allen AL von 20
			case 70: // 25% zu allen AL von 60 (tel.)
				for (Verrechnet v : lst) {
					Optional<IVerrechenbar> verrechenbar = VerrechnetService.INSTANCE.getVerrechenbar(v);
					if (verrechenbar.isPresent() && verrechenbar.get() instanceof TarmedLeistung) {
						TarmedLeistung tl = (TarmedLeistung) verrechenbar.get();
						if (tl.getCode().startsWith("00.25")) { //$NON-NLS-1$
							continue;
						}
						sum += (tl.getAL() * v.getZahl());
						// int summand = tl.getAL() >> 2; // TODO ev. float?
						// -> Rundung?
						// ((sum.addCent(summand * v.getZahl());
					}
				}
				// check.setPreis(sum.multiply(factor));
				newVerrechnet.setTP(sum);
				newVerrechnet.setDetail(AL, Double.toString(sum));
				newVerrechnet.setPrimaryScaleFactor(0.25);
				break;
			case 40: // 22-7: 180 TP
				break;
			case 50: // 50% zu allen AL von 40
			case 90: // 50% zu allen AL von 70 (tel.)
				for (Verrechnet v : lst) {
					Optional<IVerrechenbar> verrechenbar = VerrechnetService.INSTANCE.getVerrechenbar(v);
					if (verrechenbar.isPresent() && verrechenbar.get() instanceof TarmedLeistung) {
						TarmedLeistung tl = (TarmedLeistung) verrechenbar.get();
						if (tl.getCode().startsWith("00.25")) { //$NON-NLS-1$
							continue;
						}
						// int summand = tl.getAL() >> 1;
						// sum.addCent(summand * v.getZahl());
						sum += (tl.getAL() * v.getZahl());
					}
				}
				// check.setPreis(sum.multiply(factor));
				newVerrechnet.setTP(sum);
				newVerrechnet.setDetail(AL, Double.toString(sum));
				newVerrechnet.setPrimaryScaleFactor(0.5);
				break;

			case 60: // Tel. Mo-Fr 19-22, Sa 12-22, So 7-22: 30 TP
				break;
			case 80: // Tel. von 22-7: 70 TP
				break;

			}
			return ObjectStatus.OK_STATUS(newVerrechnet);
			// return new Result<IVerrechenbar>(Result.SEVERITY.OK,
			// PREISAENDERUNG, "Preis", null, false); //$NON-NLS-1$
		}
		return Status.OK_STATUS;
		// return new Result<IVerrechenbar>(null);
	}

	@Override
	public IStatus optify(Behandlung kons, String userId, String mandatorId) {
		List<TarmedLeistung> postponed = new LinkedList<TarmedLeistung>();
		for (Verrechnet vv : kons.getVerrechnet()) {
			Optional<IVerrechenbar> iv = VerrechnetService.INSTANCE.getVerrechenbar(vv);
			if (iv.isPresent() && iv.get() instanceof TarmedLeistung) {
				TarmedLeistung tl = (TarmedLeistung) iv.get();
				String tcid = tl.getCode();
				if (("35.0020".equals(tcid)) || ("04.1930".equals(tcid)) || "00.25".startsWith(tcid)) {
					postponed.add(tl);
				}
			}
		}
		return null;
	}

	/**
	 * Eine Verrechnungsposition entfernen. Der Optifier sollte prüfen, ob die
	 * Konsultation nach Entfernung dieses Codes noch konsistent verrechnet wäre
	 * und ggf. anpassen oder das Entfernen verweigern. Diese Version macht
	 * keine Prüfungen, sondern erfüllt nur die Anfrage..
	 */
	@Override
	public IStatus remove(Verrechnet code, Behandlung kons) {
		VerrechnetService.INSTANCE.delete(code);
		return Status.OK_STATUS;
	}

	private String getSide(Verrechnet v) {
		Optional<IVerrechenbar> vv = VerrechnetService.INSTANCE.getVerrechenbar(v);
		if (vv.isPresent() && vv.get() instanceof TarmedLeistung) {
			String side = (String) v.getDetail().get(SIDE);
			if (SIDE_L.equalsIgnoreCase(side)) {
				return LEFT;
			} else if (SIDE_R.equalsIgnoreCase(side)) {
				return RIGHT;
			}
		}
		return "none";
	}

	private boolean isInstance(Verrechnet v, IVerrechenbar tmpl) {
		String klasse = v.getKlasse();
		String leistungenCode = v.getLeistungenCode();

		if (klasse.equals(tmpl.getClass().getName())) {
			if (leistungenCode.equals(tmpl.getId())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * check compatibility of one tarmed with another
	 * 
	 * @param tarmedCode
	 *            the tarmed and it's parents code are check whether they have
	 *            to be excluded
	 * @param tarmed
	 *            TarmedLeistung who incompatibilities are examined
	 * @return true OK if they are compatible, WARNING if it matches an
	 *         exclusion case
	 */
	private IStatus isCompatible(TarmedLeistung tarmedCode, TarmedLeistung tarmed) {
		String notCompatible = TarmedLeistungService.getExclusionsForTarmedLeistung(tarmed, null);

		// there are some exclusions to consider
		if (!StringTool.isNothing(notCompatible)) {
			String code = tarmedCode.getCode();
			String codeParent = tarmedCode.getParent();
			for (String nc : notCompatible.split(",")) {
				if (code.equals(nc) || codeParent.startsWith(nc)) {
					return new Status(Status.WARNING, BundleConstants.BUNDLE_ID,
							tarmed.getCode() + " nicht kombinierbar mit " + code);
				}
			}
		}
		return Status.OK_STATUS;
	}

}
