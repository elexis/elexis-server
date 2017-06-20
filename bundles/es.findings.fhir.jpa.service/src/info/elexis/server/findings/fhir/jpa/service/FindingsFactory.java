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
import ch.elexis.core.findings.util.ModelUtil;
import info.elexis.server.findings.fhir.jpa.model.annotated.Condition;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;
import info.elexis.server.findings.fhir.jpa.model.annotated.Observation;
import info.elexis.server.findings.fhir.jpa.model.annotated.ProcedureRequest;
import info.elexis.server.findings.fhir.jpa.model.service.AbstractModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.ConditionModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.ConditionService;
import info.elexis.server.findings.fhir.jpa.model.service.EncounterModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.EncounterService;
import info.elexis.server.findings.fhir.jpa.model.service.ObservationModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.ObservationService;
import info.elexis.server.findings.fhir.jpa.model.service.ProcedureRequestModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.ProcedureRequestService;

public class FindingsFactory implements IFindingsFactory {

	private static Logger logger = LoggerFactory.getLogger(FindingsFactory.class);

	private EncounterService encounterService;

	private ConditionService conditionService;

	private ProcedureRequestService procedureRequestService;

	private ObservationService observationService;

	public FindingsFactory() {
		encounterService = new EncounterService();
		conditionService = new ConditionService();
		procedureRequestService = new ProcedureRequestService();
		observationService = new ObservationService();
	}

	@Override
	public IEncounter createEncounter() {
		EncounterModelAdapter ret = new EncounterModelAdapter(encounterService.create());
		org.hl7.fhir.dstu3.model.Encounter fhirEncounter = new org.hl7.fhir.dstu3.model.Encounter();
		fhirEncounter.setId(new IdType(fhirEncounter.getClass().getSimpleName(), ret.getId()));
		ModelUtil.saveResource(fhirEncounter, ret);
		saveFinding(ret);
		return ret;
	}

	@Override
	public IObservation createObservation() {
		ObservationModelAdapter ret = new ObservationModelAdapter(observationService.create());
		org.hl7.fhir.dstu3.model.Observation fhirObservation = new org.hl7.fhir.dstu3.model.Observation();
		fhirObservation.setId(new IdType(fhirObservation.getClass().getSimpleName(), ret.getId()));
		ModelUtil.saveResource(fhirObservation, ret);
		saveFinding(ret);
		return ret;
	}

	@Override
	public ICondition createCondition() {
		ConditionModelAdapter ret = new ConditionModelAdapter(conditionService.create());
		org.hl7.fhir.dstu3.model.Condition fhirCondition = new org.hl7.fhir.dstu3.model.Condition();
		fhirCondition.setId(new IdType(fhirCondition.getClass().getSimpleName(), ret.getId()));
		fhirCondition.setAssertedDate(new Date());
		ModelUtil.saveResource(fhirCondition, ret);
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
		ProcedureRequestModelAdapter ret = new ProcedureRequestModelAdapter(procedureRequestService.create());
		org.hl7.fhir.dstu3.model.ProcedureRequest fhirProcedureRequest = new org.hl7.fhir.dstu3.model.ProcedureRequest();
		fhirProcedureRequest.setId(new IdType(fhirProcedureRequest.getClass().getSimpleName(), ret.getId()));
		ModelUtil.saveResource(fhirProcedureRequest, ret);
		saveFinding(ret);
		return ret;
	}

	public void saveFinding(AbstractModelAdapter<?> finding) {
		Object model = finding.getModel();
		if (model instanceof Encounter) {
			encounterService.write((Encounter) model);
			return;
		} else if (model instanceof Condition) {
			conditionService.write((Condition) model);
			return;
		} else if (model instanceof ProcedureRequest) {
			procedureRequestService.write((ProcedureRequest) model);
			return;
		} else if (model instanceof Observation) {
			observationService.write((Observation) model);
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
			return;
		} else if (model instanceof ProcedureRequest) {
			procedureRequestService.delete((ProcedureRequest) model);
			return;
		} else if (model instanceof Observation) {
			observationService.delete((Observation) model);
			return;
		}
		logger.error("Could not delete unknown finding type [" + finding + "]");
	}

}
