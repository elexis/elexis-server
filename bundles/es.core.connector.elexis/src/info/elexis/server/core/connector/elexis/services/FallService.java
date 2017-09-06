package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.FallConstants;
import ch.rgw.tools.StringTool;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Config;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

public class FallService extends PersistenceService {

	public static class Builder extends AbstractBuilder<Fall> {
		public Builder(Kontakt patient, String label, String reason, String billingMethod) {
			object = new Fall();
			object.setPatientKontakt(patient);
			object.setBezeichnung(label);
			object.setGrund(reason);
			object.setDatumVon(LocalDate.now());
			object.setExtInfoValue(FallConstants.FLD_EXTINFO_BILLING, billingMethod);
		}
	}

	/**
	 * convenience method
	 * 
	 * @param id
	 * @return
	 */
	public static Optional<Fall> load(String id) {
		return PersistenceService.load(Fall.class, id).map(v -> (Fall) v);
	}

	public static String getAbrechnungsSystem(Fall fall) {
		String ret = fall.getExtInfoAsString(FallConstants.FLD_EXTINFO_BILLING);
		if (StringTool.isNothing(ret)) {
			String[] systeme = getAbrechnungsSysteme();
			String altGesetz = fall.getGesetz();
			if (altGesetz == null) {
				altGesetz = fall.getExtInfoAsString("xGesetz");
			}
			if (altGesetz == null) {
				altGesetz = "";
			}
			int idx = StringTool.getIndex(systeme, altGesetz);
			if (idx == -1) {
				ret = systeme[0];
			} else {
				ret = systeme[idx];
			}
			fall.setExtInfoValue(FallConstants.FLD_EXTINFO_BILLING, ret);
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
	 * Determine whether a {@link Fall} is closed, which is the case when a
	 * final date is set
	 * 
	 * @param fall
	 * @return
	 */
	public static boolean isOpen(Fall fall) {
		if (fall.getDatumBis() == null) {
			return true;
		}
		return false;
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
		String kid = fall.getExtInfoAsString(name);
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

	/**
	 * Returns a configuration constant defined for the billing system (as stored in the config table).
	 * 
	 * @param billingSystem
	 * @param constant the constant to parse, not case-sensitive
	 * @return the resp. value or <code>null</code> if none found or incorrect
	 */
	public static String getBillingSystemConstant(final String billingSystem, final String constant) {
		String[] c = getBillingSystemConstants(billingSystem);
		for (String bc : c) {
			String[] val = bc.split("=");
			if (val[0].equalsIgnoreCase(constant)) {
				return val[1];
			}
		}
		return null;
	}

}
