package info.elexis.server.findings.fhir.jpa.model.service.internal;

import java.util.List;
import java.util.Optional;

import org.eclipse.persistence.queries.ScrollableCursor;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.util.FindingsFormatUtil;
import info.elexis.server.findings.fhir.jpa.model.annotated.Condition;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter_;
import info.elexis.server.findings.fhir.jpa.model.annotated.ProcedureRequest;
import info.elexis.server.findings.fhir.jpa.model.service.AbstractModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.ConditionModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.ConditionService;
import info.elexis.server.findings.fhir.jpa.model.service.EncounterModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.EncounterService;
import info.elexis.server.findings.fhir.jpa.model.service.JPAQuery;
import info.elexis.server.findings.fhir.jpa.model.service.ProcedureRequestService;
import info.elexis.server.findings.fhir.jpa.service.FindingsService;

public class InitializationUpdate24 {

	private InitializationRunnable initializationRunnable;
	private FindingsService findingsService;

	public InitializationUpdate24(InitializationRunnable initializationRunnable,
		FindingsService findingsService){
		this.initializationRunnable = initializationRunnable;
		this.findingsService = findingsService;
	}

	public void update() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		EncounterService encounterService = new EncounterService();
		ConditionService conditionService = new ConditionService();
		ProcedureRequestService procedureRequestService = new ProcedureRequestService();

		if (initializationRunnable.isCancelled()) {
			return;
		}
		// first move Conditions with category COMPLAINT to Observation
		// with category SUBJECTIVE
		int progress = 0;
		LoggerFactory.getLogger(getClass()).info("Start converting Condition to Observation ...");
		JPAQuery<Condition> conditionQuery = modelService.getQuery(Condition.class, true);
		ScrollableCursor cursor = conditionQuery.executeAsStream();
		while (cursor.hasNext()) {
			progress++;
			Condition condition = (Condition) cursor.next();
			String origContent = condition.getContent();
			if (origContent != null && !origContent.isEmpty()
					&& !FindingsFormatUtil.isCurrentFindingsFormat(origContent)) {
				Gson gson = gsonBuilder.create();
				JsonObject jsonCondition = gson.fromJson(origContent, JsonObject.class);
				if (isConditionComplaint(jsonCondition)) {
					ConditionModelAdapter conditionModel = new ConditionModelAdapter(condition);
					IObservation observation = findingsService.create(IObservation.class);
					observation.setPatientId(condition.getPatientId());
					observation.setText(conditionModel.getText().orElse(""));
					observation.setCategory(ch.elexis.core.findings.IObservation.ObservationCategory.SOAP_SUBJECTIVE);

					Optional<Encounter> encounter = findEncounterWithCondition(condition);
					if (encounter.isPresent()) {
						Encounter foundEncounter = encounter.get();
						JsonObject removedCondition = removeConditionFromEncounter(
								gson.fromJson(foundEncounter.getContent(), JsonObject.class), condition.getId());
						foundEncounter.setContent(gson.toJson(removedCondition));
						foundEncounter = encounterService.write(foundEncounter);
						observation.setEncounter(new EncounterModelAdapter(foundEncounter));
					} else {
						LoggerFactory.getLogger(getClass()).warn(
								"Converting Condition could not find encounter of Condition " + condition.getId());
					}
					condition.setDeleted(true);
					conditionService.write(condition);
					findingsService.saveFinding((AbstractModelAdapter<?>) observation);
				}
			}
			if (progress % 1000 == 0) {
				LoggerFactory.getLogger(getClass())
						.info("Converting Condition (" + progress + "/" + cursor.size() + ")");
			}
			if (initializationRunnable.isCancelled()) {
				cursor.clear();
				cursor.close();
				return;
			}
			cursor.clear();
		}
		cursor.close();
		progress = 0;
		LoggerFactory.getLogger(getClass()).info("Converting Condition to Observation done");
		LoggerFactory.getLogger(getClass()).info("Start transforming Encounter ...");
		// transform all encounters
		JPAQuery<Encounter> encounterQuery = modelService.getQuery(Encounter.class, true);
		cursor = encounterQuery.executeAsStream();
		while (cursor.hasNext()) {
			progress++;
			Encounter encounter = (Encounter) cursor.next();
			String origContent = encounter.getContent();
			if (origContent != null && !origContent.isEmpty()
					&& !FindingsFormatUtil.isCurrentFindingsFormat(origContent)) {
				Optional<String> convertedContent = FindingsFormatUtil.convertToCurrentFindingsFormat(origContent);
				if (convertedContent.isPresent()) {
					if (!convertedContent.get().isEmpty()) {
						encounter.setContent(convertedContent.get());
						encounterService.write(encounter);
					}
				}
			}
			if (progress % 1000 == 0) {
				LoggerFactory.getLogger(getClass())
						.info("Transforming Encounter (" + progress + "/" + cursor.size() + ")");
			}
			if (initializationRunnable.isCancelled()) {
				cursor.clear();
				cursor.close();
				return;
			}
			cursor.clear();
		}
		cursor.close();
		progress = 0;
		LoggerFactory.getLogger(getClass()).info("Transforming Encounter done");
		LoggerFactory.getLogger(getClass()).info("Start transforming Condition ...");
		// transform all conditions
		conditionQuery = modelService.getQuery(Condition.class, true);
		cursor = conditionQuery.executeAsStream();
		while (cursor.hasNext()) {
			progress++;
			Condition condition = (Condition) cursor.next();
			String origContent = condition.getContent();
			if (origContent != null && !origContent.isEmpty()
					&& !FindingsFormatUtil.isCurrentFindingsFormat(origContent)) {
				Optional<String> convertedContent = FindingsFormatUtil.convertToCurrentFindingsFormat(origContent);
				if (convertedContent.isPresent()) {
					if (!convertedContent.get().isEmpty()) {
						condition.setContent(convertedContent.get());
						conditionService.write(condition);
					}
				}
			}
			if (progress % 1000 == 0) {
				LoggerFactory.getLogger(getClass())
						.info("Transforming Condition (" + progress + "/" + cursor.size() + ")");
			}
			if (initializationRunnable.isCancelled()) {
				cursor.clear();
				cursor.close();
				return;
			}
			cursor.clear();
		}
		cursor.close();
		progress = 0;
		LoggerFactory.getLogger(getClass()).info("Transforming Condition done");
		LoggerFactory.getLogger(getClass()).info("Start transforming ProcedureRequest ...");
		// transform all procedurerequests
		JPAQuery<ProcedureRequest> prodecureRequestQuery = modelService.getQuery(ProcedureRequest.class, true);
		cursor = prodecureRequestQuery.executeAsStream();
		while (cursor.hasNext()) {
			progress++;
			ProcedureRequest procedureRequest = (ProcedureRequest) cursor.next();
			String origContent = procedureRequest.getContent();
			if (origContent != null && !origContent.isEmpty()
					&& !FindingsFormatUtil.isCurrentFindingsFormat(origContent)) {
				Optional<String> convertedContent = FindingsFormatUtil.convertToCurrentFindingsFormat(origContent);
				if (convertedContent.isPresent()) {
					if (!convertedContent.get().isEmpty()) {
						procedureRequest.setContent(convertedContent.get());
						procedureRequestService.write(procedureRequest);
					}
				}
			}
			if (progress % 1000 == 0) {
				LoggerFactory.getLogger(getClass())
						.info("Transforming ProcedureRequest (" + progress + "/" + cursor.size() + ")");
			}
			if (initializationRunnable.isCancelled()) {
				cursor.clear();
				cursor.close();
				return;
			}
			cursor.clear();
		}
		cursor.close();
		progress = 0;
		LoggerFactory.getLogger(getClass()).info("Transforming ProcedureRequest done");
	}

	private Optional<Encounter> findEncounterWithCondition(Condition condition) {
		JPAQuery<Encounter> encounterQuery = modelService.getQuery(Encounter.class, true);
		encounterQuery.add(Encounter_.patientid, JPAQuery.QUERY.EQUALS, condition.getPatientId());
		List<Encounter> allEncounters = encounterQuery.execute();
		for (Encounter encounter : allEncounters) {
			if (encounter.getContent().contains(condition.getId())) {
				return Optional.of(encounter);
			}
		}
		return Optional.empty();
	}

	private JsonObject removeConditionFromEncounter(JsonObject jsonObject, String conditionId) {
		JsonElement indication = jsonObject.get("indication");
		if (indication instanceof JsonArray) {
			JsonArray indicationArray = (JsonArray) indication;
			for (JsonElement indicationRef : indicationArray) {
				if (indicationRef instanceof JsonObject) {
					JsonElement reference = ((JsonObject) indicationRef).get("reference");
					if (reference instanceof JsonPrimitive) {
						String referenceString = reference.getAsString();
						if (referenceString != null && referenceString.endsWith(conditionId)) {
							indicationArray.remove(indicationRef);
							break;
						}
					}
				}
			}
		}
		return jsonObject;
	}

	private boolean isConditionComplaint(JsonObject jsonObject) {
		JsonElement category = jsonObject.get("category");
		if (category instanceof JsonObject) {
			JsonElement coding = ((JsonObject) category).get("coding");
			if (coding instanceof JsonArray) {
				JsonArray codingArray = (JsonArray) coding;
				for (JsonElement code : codingArray) {
					if (code instanceof JsonObject) {
						JsonElement systemPrimitive = ((JsonObject) code).get("system");
						JsonElement codePrimitive = ((JsonObject) code).get("code");
						if (systemPrimitive instanceof JsonPrimitive && codePrimitive instanceof JsonPrimitive) {
							return "http://hl7.org/fhir/condition-category"
									.equalsIgnoreCase(systemPrimitive.getAsString())
									&& "complaint".equalsIgnoreCase(codePrimitive.getAsString());
						}
					}
				}
			}
		}
		return false;
	}
}
