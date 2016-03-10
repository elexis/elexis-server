package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.FallConstants;
import ch.rgw.tools.StringTool;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Config;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

public class FallService extends AbstractService<Fall> {

	public static FallService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final FallService INSTANCE = new FallService();
	}

	private FallService() {
		super(Fall.class);
	}

	/**
	 * create a {@link Fall} with the resp. mandatory attributes
	 * 
	 * @param patient
	 * @param label
	 * @param reason
	 * @param billingMethod
	 * @return
	 */
	public Fall create(Kontakt patient, String label, String reason, String billingMethod) {
		em.getTransaction().begin();
		Fall fall = create(false);
		em.merge(patient);
		fall.setPatientKontakt(patient);
		fall.setBezeichnung(label);
		fall.setGrund(reason);
		fall.setDatumVon(LocalDate.now());
		fall.getExtInfo().put(FallConstants.FLD_EXTINFO_BILLING, billingMethod);
		em.getTransaction().commit();
		return fall;
	}

	public static String getAbrechnungsSystem(Fall fall) {
		String ret = (String) fall.getExtInfo().get(FallConstants.FLD_EXTINFO_BILLING);
		if (StringTool.isNothing(ret)) {
			String[] systeme = getAbrechnungsSysteme();
			String altGesetz = fall.getGesetz();
			int idx = StringTool.getIndex(systeme, altGesetz);
			if (idx == -1) {
				ret = systeme[0];
			} else {
				ret = systeme[idx];
			}
			fall.getExtInfo().put(FallConstants.FLD_EXTINFO_BILLING, ret);
		}
		return ret;
	}

	/**
	 * Find all installed billing systems. Does not support migration of the
	 * billing systems, as defined in Elexis RCP Fall#getAbrechnungsSysteme
	 * 
	 * @return an Array with the names of all configured billing systems
	 */
	public static String[] getAbrechnungsSysteme() {
		List<Config> billingSystemNodes = ConfigService.INSTANCE.getNodes(Preferences.LEISTUNGSCODES_CFG_KEY);
		List<String> systeme = billingSystemNodes.stream().map(b -> b.getWert()).collect(Collectors.toList());
		return systeme.toArray(new String[] {});
	}

	/**
	 * Retrieve a required String Value from this billing system's definition.
	 * If no variable with that name is found, the billings system constants
	 * will be searched
	 * 
	 * @param name
	 * @return a string that might be empty but will never be null.
	 */
	public static String getRequiredString(Fall fall, String name) {
		String kid = (String) fall.getExtInfo().get(name);
		if (StringTool.isNothing(kid)) {
			kid = getBillingSystemConstant(getAbrechnungsSystem(fall), name);
		}
		return kid;
	}

	public static String[] getBillingSystemConstants(final String billingSystem) {
		String bc = ConfigService.INSTANCE.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" + billingSystem + "/constants",
				null);
		if (bc == null) {
			return new String[0];
		} else {
			return bc.split("#");
		}
	}

	public static String getBillingSystemConstant(final String billingSystem, final String constant) {
		String[] c = getBillingSystemConstants(billingSystem);
		for (String bc : c) {
			String[] val = bc.split("=");
			if (val[0].equalsIgnoreCase(constant)) {
				return val[1];
			}
		}
		return "";
	}

}
