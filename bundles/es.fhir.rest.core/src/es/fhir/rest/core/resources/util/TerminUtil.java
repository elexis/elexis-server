package es.fhir.rest.core.resources.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;

import info.elexis.server.core.connector.elexis.services.TerminService;

public class TerminUtil {

	public static Map<String, String> getAgendaAreas() {
		Map<String, String> areas = new HashMap<>();

		Set<String> agendaAreas = TerminService.getAgendaAreas();
		for (String area : agendaAreas) {
			areas.put(getIdForBereich(area), area);
		}

		return areas;
	}

	public static Optional<String> resolveAgendaAreaByScheduleId(String idPart) {
		return Optional.ofNullable(getAgendaAreas().get(idPart));
	}

	public static String getIdForBereich(String area) {
		return DigestUtils.md5Hex(area);
	}

}
