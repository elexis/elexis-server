package info.elexis.server.core.connector.elexis.services;

public class FallService extends PersistenceService2 {

//	/**
//	 * Find all installed billing systems. Does not support migration of the billing
//	 * systems, as defined in Elexis RCP Fall#getAbrechnungsSysteme
//	 * 
//	 * @return an Array with the names of all configured billing systems
//	 */
//	public static String[] getAbrechnungsSysteme() {
//		List<IConfig> billingSystemNodes = ConfigService.INSTANCE.getNodes(Preferences.LEISTUNGSCODES_CFG_KEY);
//		Set<String> systeme = billingSystemNodes.stream()
//				.map(b -> b.getKey().substring(Preferences.LEISTUNGSCODES_CFG_KEY.length() + 1))
//				.map(s -> s.substring(0, s.indexOf("/"))).collect(Collectors.toSet());
//		return systeme.toArray(new String[] {});
//	}

//	/**
//	 * Retrieve a required String Value from this billing system's definition. If no
//	 * variable with that name is found, the billings system constants will be
//	 * searched
//	 * 
//	 * @param name
//	 * @return a string that might be empty but will never be null.
//	 */
//	public static String getRequiredString(ICoverage fall, String name) {
//		String kid = (String) fall.getExtInfo(name);
//		if (StringTool.isNothing(kid)) {
//			kid = getBillingSystemConstant(fall.getBillingSystem(), name);
//		}
//		return kid;
//	}

//	public static String[] getBillingSystemConstants(final String billingSystem) {
//		String bc = ConfigService.INSTANCE.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" + billingSystem + "/constants",
//				null);
//		if (bc == null) {
//			return new String[0];
//		} else {
//			return bc.split("#");
//		}
//	}

	/**
	 * Returns a configuration constant defined for the billing system (as stored in
	 * the config table).
	 * 
	 * @param billingSystem
	 * @param constant
	 *            the constant to parse, not case-sensitive
	 * @return the resp. value or <code>null</code> if none found or incorrect
	 */
//	public static String getBillingSystemConstant(final String billingSystem, final String constant) {
//		String[] c = getBillingSystemConstants(billingSystem);
//		for (String bc : c) {
//			String[] val = bc.split("=");
//			if (val[0].equalsIgnoreCase(constant)) {
//				return val[1];
//			}
//		}
//		return null;
//	}

	/**
	 * 
	 * @param billingSystemName
	 * @param attributeName
	 * @param defaultIfNotDefined
	 * @return
	 * @since 1.6
	 */
//	public static String getConfigurationValue(String billingSystemName, String attributeName,
//			String defaultIfNotDefined) {
//		String ret = ConfigService.INSTANCE.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
//				+ billingSystemName + "/" + attributeName, defaultIfNotDefined); //$NON-NLS-1$
//		return ret;
//	}

	/**
	 * @return the {@link BillingLaw} set for the {@link BillingSystem} applied to
	 *         this {@link Fall}
	 * @since 1.6
	 */
//	public static BillingLaw getConfiguredBillingSystemLaw(String billingSystem) {
//		return BillingLaw.valueOf(getConfigurationValue(billingSystem, "defaultBillingLaw", BillingLaw.KVG.name()));
//	}

}
