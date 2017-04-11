package info.elexis.server.core.connector.elexis.services;

import org.apache.commons.lang3.StringUtils;

public class ServiceUtil {

	/**
	 * return a numeric field making sure the call will not fail on illegal
	 * values
	 * 
	 * @param in
	 *            name of the field
	 * @return the value of the field as double or 0.0 if it was null or not a
	 *         Double.
	 */
	public static double checkZeroDouble(final String input) {
		if (StringUtils.isEmpty(input)) {
			return 0.0;
		}
		try {
			return Double.parseDouble(input.trim());
		} catch (NumberFormatException ex) {
			return 0.0;
		}
	}

}
