package info.elexis.server.findings.fhir.jpa.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.IDocumentReference;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.migration.IMigratorService;
import ch.elexis.core.findings.util.FindingsServiceHolder;
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.core.findings.util.model.TransientCoding;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IDocumentStore;
import ch.elexis.core.services.IEncounterService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.text.model.Samdas;
import ch.rgw.tools.VersionedResource;

@Component
public class MigratorService implements IMigratorService {
	
	@Reference
	private IFindingsService findingsService;
	
	@Reference
	private IEncounterService encounterService;
	
	@Reference
	private List<IDocumentStore> documentStores;

	@Override
	public void migratePatientsFindings(String patientId, Class<? extends IFinding> filter,
		ICoding iCoding){
		if (patientId != null && !patientId.isEmpty()) {
			if (filter.isAssignableFrom(IEncounter.class)) {
				migratePatientEncounters(patientId);
			}
			if (filter.isAssignableFrom(ICondition.class)) {
				migratePatientCondition(patientId);
			}
			if (filter.isAssignableFrom(IDocumentReference.class)) {
				migratePatientDocuments(patientId);
			}
		}
	}
	
	@Override
	public void migrateConsultationsFindings(String consultationId,
		Class<? extends IFinding> filter){
		if (consultationId != null && !consultationId.isEmpty()) {
			if (filter.isAssignableFrom(IEncounter.class)) {
				migrateConsultationEncounter(consultationId);
			}
		}
	}
	
	/**
	 * Migrate the existing diagnose text of a patient to an {@link ICondition} instance. Migration
	 * is only performed if there is not already a diagnose in form of an {@link ICondition} present
	 * for the patient.
	 * 
	 * @param patientId
	 */
	private void migratePatientCondition(String patientId){
		Optional<IPatient> patient = CoreModelServiceHolder.get().load(patientId, IPatient.class);
		patient.ifPresent(p -> {
			String diagnosis = p.getDiagnosen();
			if (diagnosis != null && !diagnosis.isEmpty()) {
				List<ICondition> conditions =
					findingsService.getPatientsFindings(patientId, ICondition.class);
				conditions = conditions.stream().filter(iFinding -> isDiagnose(iFinding))
					.collect(Collectors.toList());
				if (conditions.isEmpty()) {
					ICondition condition = findingsService.create(ICondition.class);
					condition.setPatientId(patientId);
					condition.setCategory(ConditionCategory.PROBLEMLISTITEM);
					condition.setText(diagnosis);
					findingsService.saveFinding(condition);
				}
			}
		});
	}
	
	private boolean isDiagnose(ICondition iFinding){
		return iFinding.getCategory() == ConditionCategory.PROBLEMLISTITEM;
	}
	
	private void migratePatientDocuments(String patientId) {
		Optional<IPatient> patient = CoreModelServiceHolder.get().load(patientId, IPatient.class);
		patient.ifPresent(p -> {
			documentStores.forEach(ds -> {
				List<IDocument> documents = ds.getDocuments(patientId, null, null, null);
				documents.stream().forEach(this::migrateDocument);
			});
		});
	}

	private void migrateDocument(ch.elexis.core.model.IDocument document) {
		IPatient patient = document.getPatient();
		IQuery<IDocumentReference> query = FindingsModelServiceHolder.get().getQuery(IDocumentReference.class);
		query.and("patientid", COMPARATOR.EQUALS, patient.getId());
		query.and("documentid", COMPARATOR.EQUALS, document.getId());
		List<IDocumentReference> documents = query.execute();
		if (documents.isEmpty()) {
			createDocumentReference(document);
		} else if (document.getLastupdate() > documents.get(0).getLastupdate()) {
			updateDocument(documents.get(0), document);

		}
	}

	private void createDocumentReference(IDocument document) {
		IDocumentReference findingsDocument = FindingsServiceHolder.getiFindingsService()
				.create(IDocumentReference.class);
		findingsDocument.setDocument(document);
		findingsDocument.setPatientId(document.getPatient().getId());

		updateDocument(findingsDocument, document);
		FindingsServiceHolder.getiFindingsService().saveFinding(findingsDocument);
	}

	private void updateDocument(IDocumentReference findingsDocument, IDocument document) {
		if (document.getAuthor() != null) {
			findingsDocument.setAuthorId(document.getAuthor().getId());
		}
		if (document.getCreated() != null) {
			findingsDocument.setDate(findingsDocument.getLocalDateTime(document.getCreated()));
		}
		if (document.getCategory() != null) {
			findingsDocument.setCategory(document.getCategory().getName());
		}
		FindingsServiceHolder.getiFindingsService().saveFinding(findingsDocument);
	}

	private void migratePatientEncounters(String patientId){
		Optional<IPatient> patient = CoreModelServiceHolder.get().load(patientId, IPatient.class);
		patient.ifPresent(p -> {
			List<ch.elexis.core.model.IEncounter> encounters =
				encounterService.getAllEncountersForPatient(patient.get());
			encounters.stream().forEach(this::migrateEncounter);
		});
	}
	
	private void migrateConsultationEncounter(String consultationId){
		Optional<ch.elexis.core.model.IEncounter> encounter = CoreModelServiceHolder.get()
			.load(consultationId, ch.elexis.core.model.IEncounter.class);
		encounter.ifPresent(b -> {
			migrateEncounter(b);
		});
	}
	
	private void migrateEncounter(ch.elexis.core.model.IEncounter encounter){
		IPatient patient = encounter.getCoverage().getPatient();
		IQuery<IEncounter> query = FindingsModelServiceHolder.get().getQuery(IEncounter.class);
		query.and("patientid", COMPARATOR.EQUALS, patient.getId());
		query.and("consultationid", COMPARATOR.EQUALS, encounter.getId());
		List<IEncounter> encounters = query.execute();
		if (encounters.isEmpty()) {
			createEncounter(encounter);
		} else {
			if (ModelUtil.fixFhirResource(encounters.get(0))) {
				findingsService.saveFinding(encounters.get(0));
			}
			updateEncounter(encounters.get(0), encounter);
		}
	}
	
	private void createEncounter(ch.elexis.core.model.IEncounter encounter){
		IEncounter findingsEncounter = FindingsServiceHolder.getiFindingsService().create(IEncounter.class);
		updateEncounter(findingsEncounter, encounter);
	}
	
	private void updateEncounter(IEncounter findingsEncounter,
		ch.elexis.core.model.IEncounter encounter){
		findingsEncounter.setConsultationId(encounter.getId());
		findingsEncounter.setMandatorId(encounter.getMandator().getId());
		
		LocalDate encounterDate = encounter.getDate();
		if (encounterDate != null) {
			findingsEncounter.setStartTime(encounterDate.atStartOfDay());
			findingsEncounter.setEndTime(encounterDate.atTime(23, 59, 59));
		}
		ICoverage coverage = encounter.getCoverage();
		if (coverage != null) {
			IPatient patient = coverage.getPatient();
			if (patient != null) {
				findingsEncounter.setPatientId(patient.getId());
			}
		}
		
		VersionedResource vr = encounter.getVersionedEntry();
		if (vr != null) {
			Samdas samdas = new Samdas(vr.getHead());
			findingsEncounter.setText(samdas.getRecordText());
		}
		
		List<ICoding> coding = findingsEncounter.getType();
		if (!ModelUtil.isSystemInList(CodingSystem.ELEXIS_ENCOUNTER_TYPE.getSystem(), coding)) {
			coding.add(new TransientCoding(CodingSystem.ELEXIS_ENCOUNTER_TYPE.getSystem(), "text",
				"Nicht strukturierte Konsultation"));
			findingsEncounter.setType(coding);
		}

		findingsService.saveFinding(findingsEncounter);
	}
}
