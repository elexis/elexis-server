package info.elexis.server.findings.fhir.jpa.service;

import java.util.Date;

import org.hl7.fhir.dstu3.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.findings.IClinicalImpression;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFindingsFactory;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IProcedureRequest;
import info.elexis.server.findings.fhir.jpa.model.annotated.Condition;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;
import info.elexis.server.findings.fhir.jpa.model.service.AbstractModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.ConditionModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.ConditionService;
import info.elexis.server.findings.fhir.jpa.model.service.EncounterModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.EncounterService;
import info.elexis.server.findings.fhir.jpa.model.service.internal.FhirHelper;

public class FindingsFactory implements IFindingsFactory {

	private static Logger logger = LoggerFactory.getLogger(FindingsFactory.class);

	private FhirHelper fhirHelper = new FhirHelper();

	private EncounterService encounterService;

	private ConditionService conditionService;

	public FindingsFactory() {
		encounterService = new EncounterService();
		conditionService = new ConditionService();
	}

	@Override
	public IEncounter createEncounter() {
		EncounterModelAdapter ret = new EncounterModelAdapter(encounterService.create());
		org.hl7.fhir.dstu3.model.Encounter fhirEncounter = new org.hl7.fhir.dstu3.model.Encounter();
		fhirEncounter.setId(new IdType("Encounter", ret.getId()));
		fhirHelper.saveResource(fhirEncounter, ret);
		saveFinding(ret);
		return ret;
	}

	@Override
	public IObservation createObservation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICondition createCondition() {
		ConditionModelAdapter ret = new ConditionModelAdapter(conditionService.create());
		org.hl7.fhir.dstu3.model.Condition fhirCondition = new org.hl7.fhir.dstu3.model.Condition();
		fhirCondition.setId(new IdType("Condition", ret.getId()));
		fhirCondition.setDateRecorded(new Date());
		fhirHelper.saveResource(fhirCondition, ret);
		saveFinding(ret);
		return ret;
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

	public void saveFinding(AbstractModelAdapter<?> finding) {
		Object model = finding.getModel();
		if (model instanceof Encounter) {
			encounterService.write((Encounter) model);
			return;
		} else if (model instanceof Condition) {
			conditionService.write((Condition) model);
			return;
		}
		logger.error("Could not save unknown finding type [" + finding + "]");
	}

	public void deleteFinding(AbstractModelAdapter<?> finding) {
		Object model = finding.getModel();
		if (model instanceof Encounter) {
			encounterService.delete((Encounter) model);
			return;
		} else if (model instanceof Condition) {
			conditionService.delete((Condition) model);
		}
		logger.error("Could not delete unknown finding type [" + finding + "]");
	}

}
