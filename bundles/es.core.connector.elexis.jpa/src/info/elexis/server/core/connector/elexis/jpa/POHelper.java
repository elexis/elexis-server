package info.elexis.server.core.connector.elexis.jpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rgw.tools.StringTool;

public class POHelper {
	private static Logger log = LoggerFactory.getLogger(POHelper.class);

	/**
	 * return a numeric field making sure the call will not fail on illegal
	 * values
	 * 
	 * @param in
	 *            name of the field
	 * @return the value of the field as integer or 0 if it was null or not
	 *         nomeric.
	 */
	public static int checkZero(final Object in) {
		if (StringTool.isNothing(in)) {
			return 0;
		}
		try {
			return Integer.parseInt(((String) in).trim()); // We're sure in is a
															// String at this
															// point
		} catch (NumberFormatException ex) {
			log.warn("Error parsing number: " + ex.getMessage());
			return 0;
		}
	}
}
