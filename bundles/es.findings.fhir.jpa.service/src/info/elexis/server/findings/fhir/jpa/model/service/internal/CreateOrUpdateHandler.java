package info.elexis.server.findings.fhir.jpa.model.service.internal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.ICondition.ConditionStatus;
import ch.elexis.core.text.model.Samdas;
import ch.rgw.tools.VersionedResource;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Xid;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.types.XidQuality;
import info.elexis.server.core.connector.elexis.services.XidService;
import info.elexis.server.findings.fhir.jpa.model.annotated.Condition;
import info.elexis.server.findings.fhir.jpa.model.annotated.Condition_;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter_;
import info.elexis.server.findings.fhir.jpa.model.service.ConditionModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.EncounterModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.JPAQuery;
import info.elexis.server.findings.fhir.jpa.service.FindingsFactory;

public class CreateOrUpdateHandler {

	private FindingsFactory factory;

	public CreateOrUpdateHandler(FindingsFactory factory) {
		this.factory = factory;
	}

	public Optional<ConditionModelAdapter> createOrUpdateCondition(Kontakt patient) {
		String diagnosis = patient.getDiagnosen();
		if (diagnosis != null && !diagnosis.isEmpty()) {
			ConditionModelAdapter createdCondition = getOrCreateCondition(patient);
			updateCondition(createdCondition, diagnosis);
			return Optional.of(createdCondition);
		}
		return Optional.empty();
	}

	private void updateCondition(ConditionModelAdapter condition, String diagnosis) {
		condition.setCategory(ConditionCategory.DIAGNOSIS);
		condition.setStatus(ConditionStatus.ACTIVE);
		condition.addCoding(getDiagnoseCode(diagnosis));
		factory.saveFinding(condition);
	}

	private ICoding getDiagnoseCode(String diagnosis) {
		return new ICoding() {
			@Override
			public String getSystem() {
				return "www.elexis.info/diagnosis/codes/praxis";
			}

			@Override
			public String getCode() {
				return "freetext";
			}

			@Override
			public String getDisplay() {
				return diagnosis;
			}
		};
	}

	private ConditionModelAdapter createCondition(Kontakt patient) {
		ConditionModelAdapter condition = (ConditionModelAdapter) factory.createCondition();
		condition.setPatientId(patient.getId());
		XidService.INSTANCE.create("www.elexis.info/condition/created", "true", condition.getModel(),
				XidQuality.ASSIGNMENT_LOCAL);
		return condition;
	}

	private ConditionModelAdapter getOrCreateCondition(Kontakt patient) {
		JPAQuery<Condition> query = new JPAQuery<>(Condition.class);
		query.add(Condition_.patientid, JPAQuery.QUERY.EQUALS, patient.getId());
		List<Condition> existingConditions = query.execute();
		for (Condition condition : existingConditions) {
			Map<String, Xid> xids = condition.getXids();
			Xid xid = xids.get("www.elexis.info/condition/created");
			if (xid != null) {
				return new ConditionModelAdapter(condition);
			}
		}
		return createCondition(patient);
	}

	public List<EncounterModelAdapter> createOrUpdateEncounters(List<Behandlung> behandlungen) {
		List<EncounterModelAdapter> ret = new ArrayList<>();
		for (Behandlung behandlung : behandlungen) {
			String patientId = behandlung.getFall().getPatientKontakt().getId();
			JPAQuery<Encounter> query = new JPAQuery<>(Encounter.class);
			query.add(Encounter_.patientid, JPAQuery.QUERY.EQUALS, patientId);
			query.add(Encounter_.consultationid, JPAQuery.QUERY.EQUALS, behandlung.getId());
			List<Encounter> encounters = query.execute();
			if (encounters.isEmpty()) {
				ret.add(createEncounter(behandlung));
			} else {
				ret.add(updateEncounter(new EncounterModelAdapter(encounters.get(0)), behandlung));
			}
		}
		return ret;
	}

	public EncounterModelAdapter createEncounter(Behandlung behandlung) {
		EncounterModelAdapter encounter = (EncounterModelAdapter) factory.createEncounter();
		return updateEncounter(encounter, behandlung);
	}

	public EncounterModelAdapter updateEncounter(EncounterModelAdapter encounter, Behandlung behandlung) {
		encounter.setConsultationId(behandlung.getId());
		encounter.setServiceProviderId(behandlung.getMandant().getId());

		LocalDate encounterDate = behandlung.getDatum();
		if (encounterDate != null) {
			encounter.setEffectiveTime(encounterDate.atStartOfDay());
		}
		Fall fall = behandlung.getFall();
		if (fall != null) {
			Kontakt patient = fall.getPatientKontakt();
			if (patient != null) {
				encounter.setPatientId(patient.getId());
			}
		}

		VersionedResource vr = behandlung.getEintrag();
		if (vr != null) {
			Samdas samdas = new Samdas(vr.getHead());
			encounter.setText(samdas.getRecordText());
		}

		factory.saveFinding(encounter);
		return encounter;
	}

}
