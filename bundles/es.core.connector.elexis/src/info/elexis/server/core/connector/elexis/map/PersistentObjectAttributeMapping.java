package info.elexis.server.core.connector.elexis.map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rgw.tools.StringTool;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.KontaktService;

/**
 * Maps attributes of persistent classes only available as String to resp.
 * getters or operations returning the resp. value. <br>
 * E.g. Elexis UI: <code>Patient.get(FLD_AGE)</code> => Elexis Server:
 * <code>TypeAttributeService(Patient, "Alter")</code>
 *
 */
public class PersistentObjectAttributeMapping {

	protected static Logger log = LoggerFactory.getLogger(PersistentObjectAttributeMapping.class);

	public static String get(Kontakt patient, String value) {
		value = value.toLowerCase();
		switch (value) {
		case "geschlecht":
			return patient.getGender().name();
		case "alter":
			return Integer.toString(KontaktService.getAgeInYears(patient));
		default:
			break;
		}

		log.warn("Could not map attribute Patient@[" + value + "], returning empty string.");
		return StringTool.leer;
	}

}
