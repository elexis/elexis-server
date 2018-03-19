package es.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.codesystems.ConditionCategory;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.IdentifierSystem;
import ch.elexis.core.findings.util.ModelUtil;
import es.fhir.rest.core.test.AllTests;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;
import info.elexis.server.core.connector.elexis.services.BehandlungService;

public class ProcedureRequestTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		TestDatabaseInitializer initializer = new TestDatabaseInitializer();
		initializer.initializeBehandlung();

		client = ModelUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
	}

	@Test
	public void createProcedureRequest() {
		Condition problem = new Condition();
		problem.setCode(
				new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/sid/icpc-2", "A04", "Müdigkeit")));
		problem.setSubject(new Reference("Patient/" + AllTests.getTestDatabaseInitializer().getPatient().getId()));
		problem.addCategory().addCoding(new Coding(ConditionCategory.PROBLEMLISTITEM.getSystem(),
				ConditionCategory.PROBLEMLISTITEM.toCode(), ConditionCategory.PROBLEMLISTITEM.getDisplay()));
		MethodOutcome problemOutcome = client.create().resource(problem).execute();

		Encounter encounter = new Encounter();
		EncounterParticipantComponent participant = new EncounterParticipantComponent();
		participant.setIndividual(new Reference("Practitioner/" + TestDatabaseInitializer.getMandant().getId()));
		encounter.addParticipant(participant);
		encounter
				.setPeriod(new Period().setStart(AllTests.getDate(LocalDate.of(2016, Month.DECEMBER, 1).atStartOfDay()))
						.setEnd(AllTests.getDate(LocalDate.of(2016, Month.DECEMBER, 1).atTime(23, 59, 59))));
		encounter.setSubject(new Reference("Patient/" + AllTests.getTestDatabaseInitializer().getPatient().getId()));

		encounter.addDiagnosis().setCondition(
				new Reference(new IdType(problem.getResourceType().name(), problemOutcome.getId().getIdPart())));

		encounter.addType(new CodeableConcept()
				.addCoding(new Coding("www.elexis.info/encounter/type", "struct", "structured enconter")));

		MethodOutcome encounterOutcome = client.create().resource(encounter).execute();
		assertNotNull(encounterOutcome);
		assertTrue(encounterOutcome.getCreated());
		assertNotNull(encounterOutcome.getId());

		// add subjective to the encounter
		Observation subjective = new Observation();
		Narrative narrative = new Narrative();
		String divEncodedText = "Subjective\nTest".replaceAll("(\r\n|\r|\n)", "<br />");
		narrative.setDivAsString(divEncodedText);
		subjective.setText(narrative);
		subjective.setSubject(new Reference("Patient/" + AllTests.getTestDatabaseInitializer().getPatient().getId()));
		subjective.addCategory().addCoding(new Coding(IdentifierSystem.ELEXIS_SOAP.getSystem(),
				ObservationCategory.SOAP_SUBJECTIVE.getCode(), ObservationCategory.SOAP_SUBJECTIVE.getLocalized()));
		subjective.setContext(
				new Reference(new IdType(encounter.getResourceType().name(), encounterOutcome.getId().getIdPart())));
		MethodOutcome subjectiveOutcome = client.create().resource(subjective).execute();
		assertTrue(subjectiveOutcome.getCreated());

		// add procedure request to encounter
		ProcedureRequest procedureRequest = new ProcedureRequest();
		narrative = new Narrative();
		divEncodedText = "Procedure\nTest".replaceAll("(\r\n|\r|\n)", "<br />");
		narrative.setDivAsString(divEncodedText);
		procedureRequest.setText(narrative);
		procedureRequest.setSubject(new Reference("Patient/" + AllTests.getTestDatabaseInitializer().getPatient().getId()));
		procedureRequest
				.setContext(new Reference(
						new IdType(encounter.getResourceType().name(), encounterOutcome.getId().getIdPart())));

		MethodOutcome outcome = client.create().resource(procedureRequest).execute();
		assertNotNull(outcome);
		assertTrue(outcome.getCreated());
		assertNotNull(outcome.getId());

		ProcedureRequest readProcedureRequest = client.read().resource(ProcedureRequest.class).withId(outcome.getId())
				.execute();
		assertNotNull(readProcedureRequest);
		assertEquals(outcome.getId().getIdPart(), readProcedureRequest.getIdElement().getIdPart());
		assertEquals(procedureRequest.getSubject().getReferenceElement().getIdPart(),
				readProcedureRequest.getSubject().getReferenceElement().getIdPart());
		assertEquals(procedureRequest.getContext().getReferenceElement().getIdPart(),
				readProcedureRequest.getContext().getReferenceElement().getIdPart());
		assertTrue(readProcedureRequest.getText().getDivAsString().contains("Test"));

		// check if the consultation text has been updated
		// search by patient and date
		Bundle results = client.search().forResource(Encounter.class)
				.where(Encounter.PATIENT.hasId(AllTests.getTestDatabaseInitializer().getPatient().getId()))
				.and(Encounter.DATE.exactly()
						.day(AllTests.getDate(LocalDate.of(2016, Month.DECEMBER, 1).atStartOfDay())))
				.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		Encounter readEncounter = (Encounter) entries.get(0).getResource();
		assertNotNull(readEncounter);
		assertEquals(readEncounter.getIdElement().getIdPart(), encounterOutcome.getId().getIdPart());
		List<Identifier> identifier = readEncounter.getIdentifier();
		String consultationId = null;
		for (Identifier id : identifier) {
			if (id.getSystem().equals(IdentifierSystem.ELEXIS_CONSID.getSystem())) {
				consultationId = id.getValue();
			}
		}
		assertNotNull(consultationId);
		Optional<Behandlung> behandlung = BehandlungService.load(consultationId);
		assertTrue(behandlung.isPresent());
		assertTrue(behandlung.get().getEintrag().getHead().contains("Procedure"));
	}
}
