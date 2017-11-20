package info.elexis.server.core.connector.elexis.billable.optifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.status.ObjectStatus;
import info.elexis.server.core.connector.elexis.billable.IBillable;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarLabor2009Tarif;
import info.elexis.server.core.connector.elexis.internal.BundleConstants;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Labor2009Tarif;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Labor2009Tarif_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
import info.elexis.server.core.connector.elexis.services.ConfigService;
import info.elexis.server.core.connector.elexis.services.JPAQuery;
import info.elexis.server.core.connector.elexis.services.UserconfigService;
import info.elexis.server.core.connector.elexis.services.VerrechnetService;

public class LaborTarif2009Optifier implements IOptifier<Labor2009Tarif> {

	public static final String OPTIMIZE_ADDITION_INITDEADLINE = "30.06.2013";

	private Verrechnet newVerrechnet;
	private Logger log;

	@Override
	public IStatus add(IBillable<Labor2009Tarif> code, Behandlung kons, Kontakt userContact, Kontakt mandatorContact,
			float count) {
		IStatus status = Status.OK_STATUS;
		Object verrechnet = null;
		
		for (int i = 0; i < count; i++) {
			status = add(code, kons, userContact, mandatorContact);
			if(!status.isOK()) {
				return new ObjectStatus(status, verrechnet);
			} else {
				verrechnet = ((ObjectStatus) status).getObject();
			}
		}

		return status;
	}

	/**
	 * Add and recalculate the various possible amendments
	 */
	public IStatus add(IBillable<Labor2009Tarif> code, Behandlung kons, Kontakt userContact, Kontakt mandatorContact) {

		boolean bOptify = UserconfigService.get(userContact, Preferences.LEISTUNGSCODES_OPTIFY, true);

		if (bOptify) {
			Labor2009Tarif tarif = (Labor2009Tarif) code.getEntity();

			boolean validDate = isValidOn(tarif, kons.getDatum());
			if (!validDate) {
				return new Status(Status.ERROR, BundleConstants.BUNDLE_ID,
						code.getCode() + " (" + tarif.getGueltigVon() + "-" + tarif.getGueltigBis()
								+ ") Gültigkeit beinhaltet nicht das Konsultationsdatum " + kons.getDatum());
			}
		}

		newVerrechnet = new VerrechnetService.Builder(code, kons, 1, userContact).buildAndSave();
		IStatus res = optify(kons, userContact, mandatorContact);
		if (!res.isOK()) {
			VerrechnetService.delete(newVerrechnet);
		}
		return res;
	}

	private boolean isValidOn(Labor2009Tarif tarif, LocalDate datum) {
		LocalDate validFrom = tarif.getGueltigVon();
		LocalDate validTo = tarif.getGueltigBis();
		if (validTo == null) {
			validTo = LocalDate.of(2199, 12, 31);
		}
		if (validFrom == null || validTo == null) {
			if (log == null) {
				log = LoggerFactory.getLogger(DefaultOptifier.class);
			}
			log.warn("Invalid date values for LaborTarif2009 " + tarif.getId());
			return false;
		}
		if (datum.isEqual(validFrom) || datum.isEqual(validTo)) {
			return true;
		}
		return (datum.isAfter(validFrom) && datum.isBefore(validTo));
	}

	@Override
	public IStatus optify(Behandlung kons, Kontakt userContact, Kontakt mandatorContact) {
		try {
			boolean haveKons = false;

			LocalDate date = kons.getDatum();
			if (date.isBefore(LocalDate.of(2009, 7, 1))) {
				return new Status(Status.WARNING, BundleConstants.BUNDLE_ID, "Code not yet valid");
			}

			LocalDate deadline = ConfigService.INSTANCE.getAsDate("abrechnung/labor2009/optify/addition/deadline");

			if (deadline == null) {
				deadline = LocalDate.of(2013, 6, 30);
			}

			List<Verrechnet> list = VerrechnetService.getAllVerrechnetForBehandlung(kons);
			Verrechnet v470710 = null;
			Verrechnet v470720 = null;
			Verrechnet v4708 = null;
			int z4708 = 0;
			int z4707 = 0;
			int z470710 = 0;
			int z470720 = 0;

			for (Verrechnet v : list) {
				Optional<IBillable> iv = VerrechnetService.getVerrechenbar(v);
				if (iv.isPresent() && iv.get() instanceof Labor2009Tarif) {
					String cc = iv.get().getCode();
					if (cc.equals("4708.00")) { // Übergangszuschlag //$NON-NLS-1$
						v4708 = v;
					} else if (cc.equals("4707.00")) { // Pauschale //$NON-NLS-1$
						if (z4707 < 1) {
							z4707 = 1;
						} else {
							return new Status(Status.WARNING, BundleConstants.BUNDLE_ID, "4707.00 only once per cons");
						}
					} else if (cc.equals("4707.10")) { // Fachbereich C
						v470710 = v;
					} else if (cc.equals("4707.20")) { // Fachbereich nicht-C
						v470720 = v;
					} else if (cc.equals("4703.00") || cc.equals("4701.00") || cc.equals("4704.00")
							|| cc.equals("4706.00")) {
						continue;
					} else {
						Labor2009Tarif vlt = (Labor2009Tarif) iv.get();
						if (!isSchnellAnalyse(vlt)) {
							if (vlt.getFachbereich().indexOf("C") > -1) { //$NON-NLS-1$
								z470710 += v.getZahl();
							} else {
								z470720 += v.getZahl();
							}
						}
						z4708 += v.getZahl();
					}
				} else if (iv.get().getCode().equals("00.0010") || iv.get().getCode().equals("00.0060")) {
					// Kons erste 5 Minuten
					haveKons = true;
				}
			}
			// reduce amendments to max. 24 TP
			while (((4 + 2 * z470710 + z470720) > 26) && z470710 > 0) {
				z470710--;
			}
			while (((4 + 2 * z470710 + z470720) > 24) && z470720 > 0) {
				z470720--;
			}

			if (z470710 == 0 || haveKons == false) {
				if (v470710 != null) {
					VerrechnetService.delete(v470710);
				}
			} else {
				if (v470710 == null) {
					v470710 = doCreate(kons, "4707.10", userContact); //$NON-NLS-1$
				}
				v470710.setZahl(z470710);
			}

			if (z470720 == 0 || haveKons == false) {
				if (v470720 != null) {
					VerrechnetService.delete(v470720);
				}
			} else {
				if (v470720 == null) {
					v470720 = doCreate(kons, "4707.20", userContact); //$NON-NLS-1$
				}
				v470720.setZahl(z470720);
			}

			// only consider 4707.00 & 4708.00 before 01.01.2015
			// configured deadline is still active before 01.01.2015
			if (date.isBefore(LocalDate.of(2015, 1, 1))) {
				if (z4707 == 0 && ((z470710 + z470720) > 0) && haveKons == true) {
					doCreate(kons, "4707.00", userContact); //$NON-NLS-1$
				}
				if (z4708 > 0 && haveKons == true) {
					if (v4708 == null) {
						if (date.isBefore(deadline)) {
							v4708 = doCreate(kons, "4708.00", userContact); //$NON-NLS-1$
						}
					} else {
						if (date.isAfter(deadline) || date.isEqual(deadline)) {
							VerrechnetService.delete(v4708);
							return new Status(Status.WARNING, BundleConstants.BUNDLE_ID,
									"4708.00 only until " + deadline);
						}
					}
				}
				if (v4708 != null) {
					v4708.setZahl(z4708);
				}
			}

			return ObjectStatus.OK_STATUS(newVerrechnet);
		} catch (Exception ex) {
			log.error("Error optifying.", ex);
			return new Status(Status.ERROR, BundleConstants.BUNDLE_ID, "Tarif not installed correctly");

		}
	}

	private boolean isSchnellAnalyse(Labor2009Tarif vlt) {
		String chapter = vlt.getChapter().trim();
		if (chapter != null && !chapter.isEmpty()) {
			String[] chapters = chapter.split(",");
			for (String string : chapters) {
				if (string.trim().equals("5.1.2.2.1")) {
					return true;
				}
			}
		}
		return false;
	}

	private Verrechnet doCreate(Behandlung kons, String code, Kontakt userContact) throws Exception {
		JPAQuery<Labor2009Tarif> query = new JPAQuery<Labor2009Tarif>(Labor2009Tarif.class);
		query.add(Labor2009Tarif_.code, JPAQuery.QUERY.EQUALS, code);
		List<Labor2009Tarif> list = query.execute();
		Labor2009Tarif tarif = null;
		for (Labor2009Tarif labor2009Tarif : list) {
			if (isValidOn(labor2009Tarif, kons.getDatum())) {
				tarif = labor2009Tarif;
				break;
			}
		}

		if (tarif != null) {
			VerrechenbarLabor2009Tarif verrechenbarLabor2009Tarif = new VerrechenbarLabor2009Tarif(tarif);
			newVerrechnet = new VerrechnetService.Builder(verrechenbarLabor2009Tarif, kons, 1, userContact)
					.buildAndSave();
			return newVerrechnet;
		} else {
			throw new Exception("Tarif not installed correctly"); //$NON-NLS-1$
		}

	}

	@Override
	public IStatus remove(Verrechnet code) {
		VerrechnetService.delete(code);
		return optify(code.getBehandlung(), code.getUser(), null);
	}

}
