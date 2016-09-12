package info.elexis.server.findings.fhir.jpa.service;

import info.elexis.server.core.connector.elexis.services.AbstractService;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;

public class EncounterService extends AbstractService<Encounter> {

	public EncounterService() {
		super(Encounter.class);
	}

}
