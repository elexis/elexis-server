package es.fhir.rest.core.model.util.transformer.helper;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

public class CodeSystemHelper extends AbstractHelper {

	@SuppressWarnings("serial")
	private static HashMap<String, String> systemIdMap = new HashMap<String, String>() {
		{
			put("www.elexis.info/coverage/type", "coveragetype");
			put("www.elexis.info/diagnose/tessinercode", "tessinercode");
		}
	};

	private static boolean isSystemString(String string) {
		return string.startsWith("http://") || string.startsWith("www.elexis.info/");
	}

	public static Optional<String> getIdForString(String string) {
		if (isSystemString(string)) {
			return Optional.ofNullable(systemIdMap.get(string));
		}
		return Optional.of(string);
	}

	public static Optional<String> getSystemForId(String idString) {
		Set<String> keys = systemIdMap.keySet();
		for (String key : keys) {
			if (systemIdMap.get(key).equals(idString)) {
				return Optional.of(key);
			}
		}
		return Optional.empty();
	}
}
