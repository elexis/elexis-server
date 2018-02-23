/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package info.elexis.server.core.connector.elexis.billable.optifier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.status.ObjectStatus;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.billable.IBillable;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarTarmedLeistung;
import info.elexis.server.core.connector.elexis.billable.tarmed.TarmedExclusive;
import info.elexis.server.core.connector.elexis.billable.tarmed.TarmedKumulationType;
import info.elexis.server.core.connector.elexis.billable.tarmed.TarmedLeistungAge;
import info.elexis.server.core.connector.elexis.billable.tarmed.TarmedLimitation;
import info.elexis.server.core.connector.elexis.billable.tarmed.TarmedLimitation.LimitationUnit;
import info.elexis.server.core.connector.elexis.internal.BundleConstants;
import info.elexis.server.core.connector.elexis.jpa.ElexisTypeMap;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedExtension;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedGroup;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
import info.elexis.server.core.connector.elexis.services.ConfigService;
import info.elexis.server.core.connector.elexis.services.FallService;
import info.elexis.server.core.connector.elexis.services.KontaktService;
import info.elexis.server.core.connector.elexis.services.TarmedExclusion;
import info.elexis.server.core.connector.elexis.services.TarmedLeistungService;
import info.elexis.server.core.connector.elexis.services.UserconfigService;
import info.elexis.server.core.connector.elexis.services.VerrechnetService;

/**
 * Dies ist eine Beispielimplementation des IOptifier Interfaces, welches einige
 * einfache Checks von Tarmed-Verrechnungen durchführt
 * 
 * @author gerry
 * 
 */
public class TarmedOptifier implements IOptifier<TarmedLeistung> {
	private static final String TL = "TL"; //$NON-NLS-1$
	private static final String AL = "AL"; //$NON-NLS-1$
	private static final String AL_NOTSCALED = "AL_NOTSCALED"; //$NON-NLS-1$
	private static final String AL_SCALINGFACTOR = "AL_SCALINGFACTOR"; //$NON-NLS-1$
	public static final int OK = 0;
	public static final int PREISAENDERUNG = 1;
	public static final int KUMULATION = 2;
	public static final int KOMBINATION = 3;
	public static final int EXKLUSION = 4;
	public static final int INKLUSION = 5;
	public static final int LEISTUNGSTYP = 6;
	public static final int NOTYETVALID = 7;
	public static final int NOMOREVALID = 8;
	public static final int PATIENTAGE = 9;
	public static final int EXKLUSIVE = 10;
	public static final int EXKLUSIONSIDE = 11;

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
	private String newVerrechnetSide;

	private Map<String, Object> contextMap;

	/**
	 * Hier kann eine Behandlung als Ganzes nochmal überprüft werden
	 */
	@SuppressWarnings("rawtypes")
	public IStatus optify(Behandlung kons, Kontakt userContact, Kontakt mandatorContact) {
		List<TarmedLeistung> postponed = new LinkedList<TarmedLeistung>();
		for (Verrechnet vv : VerrechnetService.getAllVerrechnetForBehandlung(kons)) {
			Optional<IBillable> iv = VerrechnetService.getVerrechenbar(vv);
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

	@Override
	public synchronized void putContext(String key, Object value) {
		if (contextMap == null) {
			contextMap = new HashMap<String, Object>();
		}
		contextMap.put(key, value);
	}

	@Override
	public void clearContext() {
		if (contextMap != null) {
			contextMap.clear();
		}
	}

	@Override
	public IStatus add(IBillable<TarmedLeistung> code, Behandlung kons, Kontakt userContact, Kontakt mandatorContact,
			float count) {

		IStatus status = Status.OK_STATUS;
		Object verrechnet = null;

		for (int i = 0; i < count; i++) {
			status = add(code, kons, userContact, mandatorContact);
			if (!status.isOK()) {
				return new ObjectStatus(status, verrechnet);
			} else {
				verrechnet = ((ObjectStatus) status).getObject();
			}
		}

		return status;
	}

	/**
	 * Convenience method, assuming mandator = kons.getMandant(), user =
	 * kons.getMandant and count = 1;
	 * 
	 * @param code
	 * @param kons
	 * @param userContact
	 * @return
	 */
	public IStatus add(IBillable<TarmedLeistung> code, Behandlung kons) {
		return add(code, kons, kons.getMandant(), kons.getMandant(), 1);
	}

	/**
	 * Eine Verrechnungsposition zufügen. Der Optifier muss prüfen, ob die
	 * Verrechnungsposition im Kontext der übergebenen Behandlung verwendet werden
	 * kann und kann sie ggf. zurückweisen oder modifizieren.
	 */
	@SuppressWarnings("rawtypes")
	public IStatus add(IBillable<TarmedLeistung> code, Behandlung kons, Kontakt userContact, Kontakt mandatorContact) {

		bOptify = UserconfigService.get(userContact, Preferences.LEISTUNGSCODES_OPTIFY, true);

		TarmedLeistung tc = code.getEntity();
		List<Verrechnet> lst = VerrechnetService.getAllVerrechnetForBehandlung(kons);
		/*
		 * TODO Hier checken, ob dieser code mit der Dignität und Fachspezialisierung
		 * des aktuellen Mandanten usw. vereinbar ist
		 */

		Map<String, String> ext;

		TarmedExtension extension = tc.getExtension();
		if (extension == null) {
			ext = Collections.emptyMap();
		} else {
			ext = extension.getLimits();
		}

		// Gültigkeit gemäss Datum und Alter prüfen
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
			String ageLimits = ext.get(TarmedLeistung.EXT_FLD_SERVICE_AGE);
			if (ageLimits != null && !ageLimits.isEmpty()) {
				String errorMessage = checkAge(ageLimits, kons);
				if (errorMessage != null) {
					return new Status(Status.WARNING, BundleConstants.BUNDLE_ID, code.getCode() + errorMessage);
				}
			}
		}
		newVerrechnet = null;
		newVerrechnetSide = null;
		// Korrekter Fall Typ prüfen, und ggf. den code ändern
		if (tc.getCode().matches("39.002[01]") || tc.getCode().matches("39.001[0156]")) {
			String gesetz = FallService.getRequiredString(kons.getFall(), "Gesetz");
			if (gesetz == null || gesetz.isEmpty()) {
				gesetz = FallService.getAbrechnungsSystem(kons.getFall());
			}

			if (gesetz.equalsIgnoreCase("KVG") && tc.getCode().matches("39.0011")) {
				return this.add(getKonsVerrechenbar("39.0010", kons), kons);
			} else if (!gesetz.equalsIgnoreCase("KVG") && tc.getCode().matches("39.0010")) {
				return this.add(getKonsVerrechenbar("39.0011", kons), kons);
			}

			if (gesetz.equalsIgnoreCase("KVG") && tc.getCode().matches("39.0016")) {
				return this.add(getKonsVerrechenbar("39.0015", kons), kons);
			} else if (!gesetz.equalsIgnoreCase("KVG") && tc.getCode().matches("39.0015")) {
				return this.add(getKonsVerrechenbar("39.0016", kons), kons);
			}

			if (gesetz.equalsIgnoreCase("KVG") && tc.getCode().matches("39.0021")) {
				return this.add(getKonsVerrechenbar("39.0020", kons), kons);
			} else if (!gesetz.equalsIgnoreCase("KVG") && tc.getCode().matches("39.0020")) {
				return this.add(getKonsVerrechenbar("39.0021", kons), kons);
			}
		}

		if (tc.getCode().matches("35.0020")) {
			List<Verrechnet> opCodes = getOPList(lst);
			List<Verrechnet> opReduction = getVerrechnetMatchingCode(lst, "35.0020");
			// updated reductions to codes, and get not yet reduced codes
			List<Verrechnet> availableCodes = updateOPReductions(opCodes, opReduction);
			if (availableCodes.isEmpty()) {
				return new Status(Status.WARNING, BundleConstants.BUNDLE_ID, code.getCode() + " " + KOMBINATION);

			}
			for (Verrechnet verrechnet : availableCodes) {
				newVerrechnet = new VerrechnetService.Builder(code, kons, 1, userContact).build();
				mapOpReduction(verrechnet, newVerrechnet);
			}
			return ObjectStatus.OK_STATUS(newVerrechnet);
			// return new Result<IBillable>(null);
		}

		// Ist der Hinzuzufügende Code vielleicht schon in der Liste? Dann
		// nur Zahl erhöhen.
		for (Verrechnet v : lst) {
			if (isInstance(v, code)) {
				if (!tc.requiresSide()) {
					newVerrechnet = v;
					newVerrechnet.setZahl(newVerrechnet.getZahl() + 1);
					saveVerrechnet();
					break;
				}
			}
		}

		if (tc.requiresSide()) {
			newVerrechnetSide = getNewVerrechnetSideOrIncrement(code, lst);
		}

		// Ausschliessende Kriterien prüfen ("Nicht zusammen mit")
		if (newVerrechnet == null) {
			newVerrechnet = new VerrechnetService.Builder(code, kons, 1, userContact).build();
			// make sure side is initialized
			if (tc.requiresSide()) {
				newVerrechnet.setDetail(SIDE, newVerrechnetSide);
			}
			// Exclusionen
			if (bOptify) {
				TarmedLeistung newTarmed = (TarmedLeistung) code.getEntity();
				for (Verrechnet v : lst) {
					Optional<IBillable> verrechenbar = VerrechnetService.getVerrechenbar(v);
					if (verrechenbar.isPresent() && verrechenbar.get().getEntity() instanceof TarmedLeistung) {
						TarmedLeistung tarmed = (TarmedLeistung) verrechenbar.get().getEntity();
						// check if new has an exclusion for this verrechnet
						// tarmed
						IStatus resCompatible = isCompatible(v, tarmed, newVerrechnet, newTarmed, kons);
						if (resCompatible.isOK()) {
							// check if existing tarmed has exclusion for
							// new one
							resCompatible = isCompatible(newVerrechnet, newTarmed, v, tarmed, kons);
						}

						if (!resCompatible.isOK()) {
							VerrechnetService.delete(newVerrechnet);
							return resCompatible;
						}
					}
				}

				Optional<IBillable> verrechenbar = VerrechnetService.getVerrechenbar(newVerrechnet);
				if (verrechenbar.isPresent()) {
					if (verrechenbar.get().getCode().equals("00.0750")
							|| verrechenbar.get().getCode().equals("00.0010")) {
						String excludeCode = null;
						if ("00.0010".equals(verrechenbar.get().getCode())) {
							excludeCode = "00.0750";
						} else {
							excludeCode = "00.0010";
						}
						for (Verrechnet v : lst) {
							Optional<IBillable> vr = VerrechnetService.getVerrechenbar(v);
							if (vr.isPresent() && excludeCode.equals(vr.get().getCode())) {
								VerrechnetService.delete(newVerrechnet);
								return new Status(Status.WARNING, BundleConstants.BUNDLE_ID,
										"00.0750 ist nicht im Rahmen einer ärztlichen Beratung 00.0010 verrechnenbar.");
							}
						}
					}
				}
			}
			newVerrechnet.setDetail(AL, Integer.toString(TarmedLeistungService.getAL(tc, kons.getMandant())));
			setALScalingInfo(tc, newVerrechnet, kons.getMandant(), false);
			newVerrechnet.setDetail(TL, Integer.toString(tc.getTL()));
			lst.add(newVerrechnet);
		}

		// set bezug of zuschlagsleistung and referenzleistung
		if (isReferenceInfoAvailable() && shouldDetermineReference(tc)) {
			// lookup available masters
			List<Verrechnet> masters = getPossibleMasters(newVerrechnet, lst);
			if (masters.isEmpty()) {
				int zahl = newVerrechnet.getZahl();
				if (zahl > 1) {
					newVerrechnet.setZahl(zahl - 1);
					saveVerrechnet();
				} else {
					VerrechnetService.delete(newVerrechnet);
				}
				return new Status(Status.WARNING, BundleConstants.BUNDLE_ID, "Für die Zuschlagsleistung "
						+ code.getCode() + " konnte keine passende Hauptleistung gefunden werden.");
			}
			if (!masters.isEmpty()) {
				String bezug = newVerrechnet.getDetail("Bezug");
				if (bezug == null) {
					// set bezug to first available master
					newVerrechnet.setDetail("Bezug", VerrechnetService.getCode(masters.get(0)));
				} else {
					boolean found = false;
					// lookup matching, or create new Verrechnet
					for (Verrechnet mVerr : masters) {
						if (VerrechnetService.getCode(mVerr).equals(bezug)) {
							// just mark as found as amount is already increased
							found = true;
						}
					}
					if (!found) {
						// create a new Verrechnet and decrease amount
						newVerrechnet.setZahl(newVerrechnet.getZahl() - 1);
						saveVerrechnet();
						newVerrechnet = new VerrechnetService.Builder(code, kons, 1, userContact).build();
						newVerrechnet.setDetail("Bezug", VerrechnetService.getCode(masters.get(0)));
					}
				}
			}
		}

		Result<IBillable> limitResult = checkLimitations(kons, tc, newVerrechnet);
		if (!limitResult.isOK()) {
			return new Status(Status.WARNING, BundleConstants.BUNDLE_ID, limitResult.toString());
		}

		String tcid = code.getCode();

		// check if it's an X-RAY service and add default tax if so
		// default xray tax will only be added once (see above)
		if (!tc.getCode().equals(DEFAULT_TAX_XRAY_ROOM) && !tc.getCode().matches("39.002[01]")
				&& tc.getParent().startsWith(CHAPTER_XRAY)) {
			if (UserconfigService.get(userContact, Preferences.LEISTUNGSCODES_OPTIFY_XRAY, true)) {
				saveVerrechnet();
				add(getKonsVerrechenbar(DEFAULT_TAX_XRAY_ROOM, kons), kons);
				// add 39.0020, will be changed according to case (see above)
				saveVerrechnet();
				add(getKonsVerrechenbar("39.0020", kons), kons);
			}
		}

		// Interventionelle Schmerztherapie: Zuschlag cervical und thoracal
		else if (tcid.equals("29.2090")) {
			double sumAL = 0.0;
			double sumTL = 0.0;
			for (Verrechnet v : lst) {
				Optional<IBillable> verrechenbar = VerrechnetService.getVerrechenbar(v);
				if (verrechenbar.isPresent() && verrechenbar.get().getEntity() instanceof TarmedLeistung) {
					TarmedLeistung tl = (TarmedLeistung) verrechenbar.get().getEntity();
					String tlc = tl.getCode();
					double z = v.getZahl();
					if (tlc.matches("29.20[12345678]0") || (tlc.equals("29.2200"))) {
						sumAL += (z * TarmedLeistungService.getAL(tl, kons.getMandant())) / 2;
						sumTL += (z * tl.getTL()) / 4;
					}
				}
			}
			newVerrechnet.setTP(sumAL + sumTL);
			newVerrechnet.setDetail(AL, Double.toString(sumAL));
			newVerrechnet.setDetail(TL, Double.toString(sumTL));
		}

		// Zuschlag Kinder
		else if (tcid.equals("00.0010") || tcid.equals("00.0060")) {
			boolean result = UserconfigService.get(mandatorContact, PREF_ADDCHILDREN, false);

			if (result) {
				Fall f = kons.getFall();
				if (f != null) {
					Kontakt p = f.getPatientKontakt();
					if (p != null) {
						int alter = KontaktService.getAgeInYears(p);
						if (alter >= 0 && alter < 6) {
							TarmedLeistung tl = (TarmedLeistung) getKonsVerrechenbar("00.0040", kons);
							saveVerrechnet();
							add(new VerrechenbarTarmedLeistung(tl), kons, userContact, mandatorContact);
						}
					}
				}
			}
		}

		// Zuschläge für Insellappen 50% auf AL und TL bei 1910,20,40,50
		else if (tcid.equals("04.1930")) { //$NON-NLS-1$
			double sumAL = 0.0;
			double sumTL = 0.0;
			for (Verrechnet v : lst) {
				Optional<IBillable> verrechenbar = VerrechnetService.getVerrechenbar(v);
				if (verrechenbar.isPresent() && verrechenbar.get().getEntity() instanceof TarmedLeistung) {
					TarmedLeistung tl = (TarmedLeistung) verrechenbar.get().getEntity();
					String tlc = tl.getCode();
					int z = v.getZahl();
					if (tlc.equals("04.1910") || tlc.equals("04.1920") || tlc.equals("04.1940") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							|| tlc.equals("04.1950")) { //$NON-NLS-1$
						sumAL += TarmedLeistungService.getAL(tl, kons.getMandant()) * z;
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
		// Zuschläge für 04.0620 sollte sich diese mit 70% auf die Positionen 04.0630 &
		// 04.0640 beziehen
		else if (tcid.equals("04.0620")) {
			double sumAL = 0.0;
			double sumTL = 0.0;
			for (Verrechnet v : lst) {
				Optional<IBillable> verrechenbar = VerrechnetService.getVerrechenbar(v);
				if (verrechenbar.isPresent() && verrechenbar.get().getEntity() instanceof TarmedLeistung) {
					TarmedLeistung tl = (TarmedLeistung) verrechenbar.get().getEntity();
					String tlc = tl.getCode();
					int z = v.getZahl();
					if (tlc.equals("04.0610") || tlc.equals("04.0630") || tlc.equals("04.0640")) {
						sumAL += TarmedLeistungService.getAL(tl, kons.getMandant()) * z;
						sumTL += tl.getTL() * z;
					}
				}
			}
			newVerrechnet.setTP(sumAL + sumTL);
			newVerrechnet.setDetail(AL, Double.toString(sumAL));
			newVerrechnet.setDetail(TL, Double.toString(sumTL));
			newVerrechnet.setPrimaryScaleFactor(0.7);
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
					Optional<IBillable> verrechenbar = VerrechnetService.getVerrechenbar(v);
					if (verrechenbar.isPresent() && verrechenbar.get().getEntity() instanceof TarmedLeistung) {
						TarmedLeistung tl = (TarmedLeistung) verrechenbar.get().getEntity();
						if (tl.getCode().startsWith("00.25")) { //$NON-NLS-1$
							continue;
						}
						sum += (TarmedLeistungService.getAL(tl, kons.getMandant()) * v.getZahl());
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
					Optional<IBillable> verrechenbar = VerrechnetService.getVerrechenbar(v);
					if (verrechenbar.isPresent() && verrechenbar.get().getEntity() instanceof TarmedLeistung) {
						TarmedLeistung tl = (TarmedLeistung) verrechenbar.get().getEntity();
						if (tl.getCode().startsWith("00.25")) { //$NON-NLS-1$
							continue;
						}
						// int summand = tl.getAL() >> 1;
						// sum.addCent(summand * v.getZahl());
						sum += (TarmedLeistungService.getAL(tl, kons.getMandant()) * v.getZahl());
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
		}

		saveVerrechnet();

		return ObjectStatus.OK_STATUS(newVerrechnet);
	}

	private boolean isContext(String key) {
		return getContextValue(key) != null;
	}

	private Object getContextValue(String key) {
		if (contextMap != null) {
			return contextMap.get(key);
		}
		return null;
	}
	
	/**
	 * If there is a AL scaling used to calculate the AL value, provide original AL and AL scaling
	 * factor in the ExtInfo of the {@link Verrechnet}.
	 * 
	 * @param tarmed
	 * @param verrechnet
	 * @param mandant
	 */
	private void setALScalingInfo(TarmedLeistung tarmed, Verrechnet verrechnet, Kontakt mandant,
		boolean isComposite){
		double scaling = tarmed.getALScaling(mandant);
		if (scaling != 100) {
			newVerrechnet.setDetail(AL_NOTSCALED, Integer.toString(tarmed.getAL()));
			newVerrechnet.setDetail(AL_SCALINGFACTOR, Double.toString(scaling / 100));
		}
	}
	
	/**
	 * Get double as int rounded half up.
	 * 
	 * @param value
	 * @return
	 */
	private int doubleToInt(double value){
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(0, RoundingMode.HALF_UP);
		return bd.intValue();
	}

	@SuppressWarnings("rawtypes")
	private Result<IBillable> checkLimitations(Behandlung kons, TarmedLeistung tarmedLeistung,
			Verrechnet newVerrechnet) {
		if (bOptify) {
			// service limitations
			List<TarmedLimitation> limitations = TarmedLeistungService.getLimitations(tarmedLeistung);
			for (TarmedLimitation tarmedLimitation : limitations) {
				if (tarmedLimitation.isTestable()) {
					Result<IBillable> result = tarmedLimitation.test(kons, newVerrechnet);
					if (!result.isOK()) {

						return result;
					}
				}
			}
			// group limitations
			TimeTool date = new TimeTool(kons.getDatum());
			List<String> groups = tarmedLeistung.getServiceGroups(date);
			for (String groupName : groups) {
				Optional<TarmedGroup> group = TarmedLeistungService.findTarmedGroup(groupName, tarmedLeistung.getLaw(),
						date);
				if (group.isPresent()) {
					limitations = TarmedLeistungService.getLimitations(group.get());
					for (TarmedLimitation tarmedLimitation : limitations) {
						if (tarmedLimitation.isTestable()) {
							Result<IBillable> result = tarmedLimitation.test(kons, newVerrechnet);
							if (!result.isOK()) {
								return result;
							}
						}
					}
				}
			}
		}
		return new Result<IBillable>(null);
	}

	private String checkAge(String limitsString, Behandlung kons) {
		LocalDateTime consDate = new TimeTool(kons.getDatum()).toLocalDateTime();
		Kontakt patient = kons.getFall().getPatient();
		if (patient.getDob() == null) {
			return "Patienten Alter nicht ok, kein Geburtsdatum angegeben";
		}
		long patientAgeDays = patient.getAgeAt(consDate, ChronoUnit.DAYS);

		List<TarmedLeistungAge> ageLimits = TarmedLeistungAge.of(limitsString, consDate);
		for (TarmedLeistungAge tarmedLeistungAge : ageLimits) {
			if (tarmedLeistungAge.isValidOn(consDate.toLocalDate())) {
				// if only one of the limits is set, check only that limit
				if (tarmedLeistungAge.getFromDays() >= 0 && !(tarmedLeistungAge.getToDays() >= 0)) {
					if (patientAgeDays < tarmedLeistungAge.getFromDays()) {
						return "Patient ist zu jung, verrechenbar ab " + tarmedLeistungAge.getFromText();
					}
				} else if (tarmedLeistungAge.getToDays() >= 0 && !(tarmedLeistungAge.getFromDays() >= 0)) {
					if (patientAgeDays > tarmedLeistungAge.getToDays()) {
						return "Patient ist zu alt, verrechenbar bis " + tarmedLeistungAge.getToText();
					}
				} else if (tarmedLeistungAge.getToDays() >= 0 && tarmedLeistungAge.getFromDays() >= 0) {
					if (tarmedLeistungAge.getToDays() < tarmedLeistungAge.getFromDays()) {
						if (patientAgeDays > tarmedLeistungAge.getToDays()
								&& patientAgeDays < tarmedLeistungAge.getFromDays()) {
							return "Patienten Alter nicht ok, verrechenbar " + tarmedLeistungAge.getText();
						}
					} else {
						if (patientAgeDays > tarmedLeistungAge.getToDays()
								|| patientAgeDays < tarmedLeistungAge.getFromDays()) {
							return "Patienten Alter nicht ok, verrechenbar " + tarmedLeistungAge.getText();
						}
					}
				}
			}
		}
		return null;
	}

	private IBillable<TarmedLeistung> getKonsVerrechenbar(String code, Behandlung kons) {
		TimeTool date = new TimeTool(kons.getDatum());
		if (kons.getFall() != null) {
			String law = FallService.getRequiredString(kons.getFall(), "Gesetz");
			return TarmedLeistungService.getVerrechenbarFromCode(code, date, law).get();
		}
		return null;
	}

	private boolean isReferenceInfoAvailable() {
		return ConfigService.INSTANCE.get("ch.elexis.data.importer.TarmedReferenceDataImporter/referenceinfoavailable",
				false);
	}

	private boolean shouldDetermineReference(TarmedLeistung tc) {
		String typ = tc.getServiceTyp();
		boolean becauseOfType = typ.equals("Z");
		if (becauseOfType) {
			String text = tc.getText();
			return text.startsWith("+") || text.startsWith("-");
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	private List<Verrechnet> getAvailableMasters(TarmedLeistung slave, List<Verrechnet> lst) {
		List<Verrechnet> ret = new LinkedList<Verrechnet>();
		TimeTool konsDate = null;
		for (Verrechnet v : lst) {
			if (konsDate == null) {
				konsDate = new TimeTool(v.getBehandlung().getDatum());
			}
			Optional<IBillable> verrechenbar = VerrechnetService.getVerrechenbar(v);
			if (verrechenbar.isPresent() && verrechenbar.get().getEntity() instanceof TarmedLeistung) {
				TarmedLeistung tl = (TarmedLeistung) verrechenbar.get().getEntity();
				if (tl.getHierarchy(konsDate).contains(slave.getCode())) {
					ret.add(v);
				}
			}
		}
		return ret;
	}

	private List<Verrechnet> getPossibleMasters(Verrechnet newSlave, List<Verrechnet> lst) {
		TarmedLeistung slaveTarmed = (TarmedLeistung) VerrechnetService.getVerrechenbar(newSlave).get().getEntity();
		// lookup available masters
		List<Verrechnet> masters = getAvailableMasters(slaveTarmed, lst);
		// check which masters are left to be referenced
		int maxPerMaster = getMaxPerMaster(slaveTarmed);
		if (maxPerMaster > 0) {
			Map<Verrechnet, List<Verrechnet>> masterSlavesMap = getMasterToSlavesMap(newSlave, lst);
			for (Verrechnet master : masterSlavesMap.keySet()) {
				int masterCount = master.getZahl();
				int slaveCount = 0;
				for (Verrechnet slave : masterSlavesMap.get(master)) {
					slaveCount += slave.getZahl();
					if (slave.equals(newSlave)) {
						slaveCount--;
					}
				}
				if (masterCount <= (slaveCount * maxPerMaster)) {
					masters.remove(master);
				}
			}
		}
		return masters;
	}

	/**
	 * Creates a map of masters associated to slaves by the Bezug. This map will not
	 * contain the newSlave, as it has no Bezug set yet.
	 * 
	 * @param newSlave
	 * @param lst
	 * @return
	 */
	private Map<Verrechnet, List<Verrechnet>> getMasterToSlavesMap(Verrechnet newSlave, List<Verrechnet> lst) {
		Map<Verrechnet, List<Verrechnet>> ret = new HashMap<>();
		TarmedLeistung slaveTarmed = (TarmedLeistung) (TarmedLeistung) VerrechnetService.getVerrechenbar(newSlave).get()
				.getEntity();
		// lookup available masters
		List<Verrechnet> masters = getAvailableMasters(slaveTarmed, lst);
		for (Verrechnet verrechnet : masters) {
			ret.put(verrechnet, new ArrayList<Verrechnet>());
		}
		// lookup other slaves with same code
		List<Verrechnet> slaves = getVerrechnetMatchingCode(lst, VerrechnetService.getCode(newSlave));
		// add slaves to separate master list
		for (Verrechnet slave : slaves) {
			String bezug = slave.getDetail("Bezug");
			if (bezug != null && !bezug.isEmpty()) {
				for (Verrechnet master : ret.keySet()) {
					if (VerrechnetService.getCode(master).equals(bezug)) {
						ret.get(master).add(slave);
					}
				}
			}
		}
		return ret;
	}

	private int getMaxPerMaster(TarmedLeistung slave) {
		List<TarmedLimitation> limits = TarmedLeistungService.getLimitations(slave);
		for (TarmedLimitation limit : limits) {
			if (limit.getLimitationUnit() == LimitationUnit.MAINSERVICE) {
				// only an integer makes sense here
				return (int) limit.getAmount();
			}
		}
		// default to unknown
		return -1;
	}

	/**
	 * Create a new mapping between an OP I reduction (35.0020) and a service from
	 * the OP I section.
	 * 
	 * @param opVerrechnet
	 *            Verrechnet representing a service from the OP I section
	 * @param reductionVerrechnet
	 *            Verrechnet representing the OP I reduction (35.0020)
	 */
	private void mapOpReduction(Verrechnet opVerrechnet, Verrechnet reductionVerrechnet) {
		TarmedLeistung opVerrechenbar = (TarmedLeistung) VerrechnetService.getVerrechenbar(opVerrechnet).get()
				.getEntity();
		reductionVerrechnet.setZahl(opVerrechnet.getZahl());
		reductionVerrechnet.setDetail(TL, Double.toString(opVerrechenbar.getTL()));
		reductionVerrechnet.setDetail(AL, Double.toString(0.0));
		reductionVerrechnet.setTP(opVerrechenbar.getTL());
		reductionVerrechnet.setPrimaryScaleFactor(-0.4);
		reductionVerrechnet.setDetail("Bezug", opVerrechenbar.getCode());
		VerrechnetService.save(reductionVerrechnet);
	}

	/**
	 * Update existing OP I reductions (35.0020), and return a list of all not yet
	 * mapped OP I services.
	 * 
	 * @param opCodes
	 *            list of all available OP I codes see {@link #getOPList(List)}
	 * @param opReduction
	 *            list of all available reduction codes see
	 *            {@link #getVerrechnetMatchingCode(List)}
	 * @return list of not unmapped OP I codes
	 */
	private List<Verrechnet> updateOPReductions(List<Verrechnet> opCodes, List<Verrechnet> opReduction) {
		List<Verrechnet> notMappedCodes = new ArrayList<Verrechnet>();
		notMappedCodes.addAll(opCodes);
		// update already mapped
		for (Verrechnet reductionVerrechnet : opReduction) {
			boolean isMapped = false;
			String bezug = reductionVerrechnet.getDetail("Bezug");
			if (bezug != null && !bezug.isEmpty()) {
				for (Verrechnet opVerrechnet : opCodes) {
					TarmedLeistung opVerrechenbar = (TarmedLeistung) VerrechnetService.getVerrechenbar(opVerrechnet)
							.get().getEntity();
					String opCodeString = opVerrechenbar.getCode();
					if (bezug.equals(opCodeString)) {
						// update
						reductionVerrechnet.setZahl(opVerrechnet.getZahl());
						reductionVerrechnet.setDetail(TL, Double.toString(opVerrechenbar.getTL()));
						reductionVerrechnet.setDetail(AL, Double.toString(0.0));
						reductionVerrechnet.setPrimaryScaleFactor(-0.4);
						notMappedCodes.remove(opVerrechnet);
						isMapped = true;
						break;
					}
				}
			}
			if (!isMapped) {
				reductionVerrechnet.setZahl(0);
				reductionVerrechnet.setDetail("Bezug", "");
				saveVerrechnet();
			}
		}

		return notMappedCodes;
	}

	@SuppressWarnings("rawtypes")
	private List<Verrechnet> getOPList(List<Verrechnet> lst) {
		List<Verrechnet> ret = new ArrayList<Verrechnet>();
		for (Verrechnet v : lst) {
			Optional<IBillable> verrechenbar = VerrechnetService.getVerrechenbar(v);
			if (verrechenbar.isPresent() && verrechenbar.get().getEntity() instanceof TarmedLeistung) {
				TarmedLeistung tl = (TarmedLeistung) verrechenbar.get().getEntity();
				if (TarmedLeistungService.getSparteAsText(tl).equals("OP I")) { //$NON-NLS-1$
					ret.add(v);
				}
			}
		}
		return ret;
	}

	@SuppressWarnings("rawtypes")
	private List<Verrechnet> getVerrechnetMatchingCode(List<Verrechnet> lst, String code) {
		List<Verrechnet> ret = new ArrayList<Verrechnet>();
		for (Verrechnet v : lst) {
			Optional<IBillable> verrechenbar = VerrechnetService.getVerrechenbar(v);
			if (verrechenbar.isPresent() && verrechenbar.get().getEntity() instanceof TarmedLeistung) {
				TarmedLeistung tl = (TarmedLeistung) verrechenbar.get().getEntity();
				if (tl.getCode().equals(code)) { // $NON-NLS-1$
					ret.add(v);
				}
			}
		}
		return ret;
	}

	@SuppressWarnings("rawtypes")
	private List<Verrechnet> getVerrechnetWithBezugMatchingCode(List<Verrechnet> lst, String code) {
		List<Verrechnet> ret = new ArrayList<Verrechnet>();
		for (Verrechnet v : lst) {
			Optional<IBillable> verrechenbar = VerrechnetService.getVerrechenbar(v);
			if (verrechenbar.isPresent() && verrechenbar.get().getEntity() instanceof TarmedLeistung) {
				if (code.equals(v.getDetail("Bezug"))) { //$NON-NLS-1$
					ret.add(v);
				}
			}
		}
		return ret;
	}

	/**
	 * Always toggle the side of a specific code. Starts with left, then right, then
	 * add to the respective side.
	 * 
	 * @param code
	 * @param lst
	 * @return
	 */
	private String getNewVerrechnetSideOrIncrement(IBillable<TarmedLeistung> code, List<Verrechnet> lst) {
		int countSideLeft = 0;
		Verrechnet leftVerrechnet = null;
		int countSideRight = 0;
		Verrechnet rightVerrechnet = null;

		for (Verrechnet v : lst) {
			if (isInstance(v, code)) {
				String side = v.getDetail(SIDE);
				if (side.equals(SIDE_L)) {
					countSideLeft += v.getZahl();
					leftVerrechnet = v;
				} else {
					countSideRight += v.getZahl();
					rightVerrechnet = v;
				}
			}
		}

		// if side is provided by context use that side
		if (isContext(SIDE)) {
			String side = (String) getContextValue(SIDE);
			if (SIDE_L.equals(side) && countSideLeft > 0) {
				newVerrechnet = leftVerrechnet;
				newVerrechnet.setZahl(newVerrechnet.getZahl() + 1);
			} else if (SIDE_R.equals(side) && countSideRight > 0) {
				newVerrechnet = rightVerrechnet;
				newVerrechnet.setZahl(newVerrechnet.getZahl() + 1);
			}
			return side;
		}
		// toggle side if no side provided by context
		if (countSideLeft > 0 || countSideRight > 0) {
			if ((countSideLeft > countSideRight) && rightVerrechnet != null) {
				newVerrechnet = rightVerrechnet;
				newVerrechnet.setZahl(newVerrechnet.getZahl() + 1);
				saveVerrechnet();
			} else if ((countSideLeft <= countSideRight) && leftVerrechnet != null) {
				newVerrechnet = leftVerrechnet;
				newVerrechnet.setZahl(newVerrechnet.getZahl() + 1);
				saveVerrechnet();
			} else if ((countSideLeft > countSideRight) && rightVerrechnet == null) {
				return SIDE_R;
			}
		}
		return SIDE_L;
	}

	public IStatus isCompatible(TarmedLeistung tarmedCode, TarmedLeistung tarmed, Behandlung kons) {
		return isCompatible(null, tarmedCode, null, tarmed, kons);
	}

	/**
	 * check compatibility of one tarmed with another
	 * 
	 * @param tarmedCodeVerrechnet
	 *            the {@link Verrechnet} representing tarmedCode
	 * @param tarmedCode
	 *            the tarmed and it's parents code are check whether they have to be
	 *            excluded
	 * @param tarmedVerrechnet
	 *            the {@link Verrechnet} representing tarmed
	 * @param tarmed
	 *            TarmedLeistung who incompatibilities are examined
	 * @param kons
	 *            {@link Behandlung} providing context
	 * @return true OK if they are compatible, WARNING if it matches an exclusion
	 *         case
	 */
	public IStatus isCompatible(Verrechnet tarmedCodeVerrechnet, TarmedLeistung tarmedCode, Verrechnet tarmedVerrechnet,
			TarmedLeistung tarmed, Behandlung kons) {
		TimeTool date = new TimeTool(kons.getDatum());
		List<TarmedExclusion> exclusions = TarmedLeistungService.getExclusions(tarmed, kons);
		for (TarmedExclusion tarmedExclusion : exclusions) {
			if (tarmedExclusion.isMatching(tarmedCode, date)) {
				// exclude only if side matches
				if (tarmedExclusion.isValidSide() && tarmedCodeVerrechnet != null && tarmedVerrechnet != null) {
					String tarmedCodeSide = tarmedCodeVerrechnet.getDetail(SIDE);
					String tarmedSide = tarmedVerrechnet.getDetail(SIDE);
					if (tarmedSide != null && tarmedCodeSide != null) {
						if (tarmedSide.equals(tarmedCodeSide)) {
							return new Status(Status.WARNING, BundleConstants.BUNDLE_ID,
									tarmed.getCode() + " nicht kombinierbar mit " + tarmedExclusion.toString()
											+ " auf der selben Seite");
						} else {
							// no exclusion due to different side
							continue;
						}
					}
				}
				return new Status(Status.WARNING, BundleConstants.BUNDLE_ID,
						tarmed.getCode() + " nicht kombinierbar mit " + tarmedExclusion.toString());
			}
		}
		List<String> groups = tarmed.getServiceGroups(date);
		for (String groupName : groups) {
			Optional<TarmedGroup> group = TarmedLeistungService.findTarmedGroup(groupName, tarmed.getLaw(), date);
			if (group.isPresent()) {
				List<TarmedExclusion> groupExclusions = TarmedLeistungService.getExclusions(group.get(), kons);
				for (TarmedExclusion tarmedExclusion : groupExclusions) {
					if (tarmedExclusion.isMatching(tarmedCode, date)) {
						return new Status(Status.WARNING, BundleConstants.BUNDLE_ID,
								tarmed.getCode() + " nicht kombinierbar mit " + tarmedExclusion.toString());
					}
				}
			}
		}
		List<String> blocks = tarmed.getServiceBlocks(date);
		for (String blockName : blocks) {
			if (skipBlockExclusives(blockName)) {
				continue;
			}
			List<TarmedExclusive> exclusives = TarmedLeistungService.getExclusives(blockName,
					TarmedKumulationType.BLOCK, date, tarmed.getLaw());
			// currently only test blocks exclusives, exclude hierarchy matches
			if (canHandleAllExculives(exclusives) && !isMatchingHierarchy(tarmedCode, tarmed, date)) {
				boolean included = false;
				for (TarmedExclusive tarmedExclusive : exclusives) {
					if (tarmedExclusive.isMatching(tarmedCode, date)) {
						included = true;
					}
				}
				if (!included) {
					return new Status(Status.WARNING, BundleConstants.BUNDLE_ID, tarmed.getCode()
							+ " nicht kombinierbar mit " + tarmedCode.getCode() + ", wegen Block Kumulation");
				}
			}
		}
		return Status.OK_STATUS;
	}

	private boolean skipBlockExclusives(String blockName) {
		try {
			Integer blockNumber = Integer.valueOf(blockName);
			if (blockNumber > 50 && blockNumber < 60) {
				return true;
			}
		} catch (NumberFormatException nfe) {
			// ignore and do not skip
		}
		return false;
	}

	private boolean isMatchingHierarchy(TarmedLeistung tarmedCode, TarmedLeistung tarmed, TimeTool date) {
		return tarmed.getHierarchy(date).contains(tarmedCode.getCode());
	}

	/**
	 * Test if we can handle all {@link TarmedExclusive}.
	 * 
	 * @param exclusives
	 * @return
	 */
	private boolean canHandleAllExculives(List<TarmedExclusive> exclusives) {
		for (TarmedExclusive tarmedExclusive : exclusives) {
			if (tarmedExclusive.getSlaveType() != TarmedKumulationType.BLOCK
					&& tarmedExclusive.getSlaveType() != TarmedKumulationType.CHAPTER
					&& tarmedExclusive.getSlaveType() != TarmedKumulationType.SERVICE) {
				return false;
			}
		}
		return true;
	}

	private void saveVerrechnet() {
		if (newVerrechnet != null) {
			newVerrechnet = (Verrechnet) VerrechnetService.save(newVerrechnet);
		} else {
			LoggerFactory.getLogger(TarmedOptifier.class).warn("Call on null", new Throwable("Diagnosis"));
		}
	}

	/**
	 * Eine Verrechnungsposition entfernen. Der Optifier sollte prüfen, ob die
	 * Konsultation nach Entfernung dieses Codes noch konsistent verrechnet wäre und
	 * ggf. anpassen oder das Entfernen verweigern. Diese Version macht keine
	 * Prüfungen, sondern erfüllt nur die Anfrage..
	 */
	@Override
	public IStatus remove(Verrechnet code) {
		Behandlung behandlung = code.getBehandlung();
		List<Verrechnet> l = behandlung.getLeistungen();
		l.remove(code);
		VerrechnetService.delete(code);
		// if no more left, check for bezug and remove
		List<Verrechnet> left = getVerrechnetMatchingCode(l, VerrechnetService.getCode(code));
		if (left.isEmpty()) {
			List<Verrechnet> verrechnetWithBezug = getVerrechnetWithBezugMatchingCode(behandlung.getLeistungen(),
					VerrechnetService.getCode(code));
			for (Verrechnet verrechnet : verrechnetWithBezug) {
				remove(verrechnet);
			}
		}

		return Status.OK_STATUS;
	}

	@SuppressWarnings("rawtypes")
	private boolean isInstance(Verrechnet v, IBillable tmpl) {
		String klasse = v.getKlasse();
		String leistungenCode = v.getLeistungenCode();

		String keyForObject = ElexisTypeMap.getKeyForObject((AbstractDBObjectIdDeleted) tmpl.getEntity());

		if (klasse.equals(keyForObject)) {
			if (leistungenCode.equals(tmpl.getId())) {
				return true;
			}
		}

		return false;
	}

}
