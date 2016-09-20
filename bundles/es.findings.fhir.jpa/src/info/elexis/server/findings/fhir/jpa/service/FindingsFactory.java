package info.elexis.server.findings.fhir.jpa.service;

import org.hl7.fhir.dstu3.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.findings.IClinicalImpression;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsFactory;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IProcedureRequest;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;
import info.elexis.server.findings.fhir.jpa.model.helper.FhirHelper;

public class FindingsFactory implements IFindingsFactory {

	private static Logger logger = LoggerFactory.getLogger(FindingsFactory.class);

	private FhirHelper fhirHelper = new FhirHelper();

	private EncounterService encounterService;

	public FindingsFactory() {
		encounterService = new EncounterService();
	}

	@Override
	public IEncounter createEncounter() {
		Encounter ret = encounterService.create();
		org.hl7.fhir.dstu3.model.Encounter fhirEncounter = new org.hl7.fhir.dstu3.model.Encounter();
		fhirEncounter.setId(new IdType("Encounter", ret.getId()));
		fhirHelper.saveResource(fhirEncounter, ret);
		encounterService.write(ret);
		return ret;
	}

	@Override
	public IObservation createObservation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICondition createCondition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IClinicalImpression createClinicalImpression() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IProcedureRequest createProcedureRequest() {
		// TODO Auto-generated method stub
		return null;
	}

	public void saveFinding(IFinding finding) {
		if (finding instanceof Encounter) {
			encounterService.write((Encounter) finding);
			return;
		}
		logger.error("Could not save unknown finding type [" + finding + "]");
	}

	public void deleteFinding(IFinding finding) {
		if (finding instanceof Encounter) {
			encounterService.delete((Encounter) finding);
			encounterService.write((Encounter) finding);
			return;
		}
		logger.error("Could not delete unknown finding type [" + finding + "]");
	}

}
