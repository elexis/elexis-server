package es.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationReferenceRangeComponent;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.exceptions.FHIRException;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.IGenericClient;
import ch.elexis.core.findings.IFindingsFactory;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.IObservation.ObservationCode;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.core.findings.util.model.TransientCoding;
import es.fhir.rest.core.test.AllTests;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;

public class ObservationTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		TestDatabaseInitializer initializer = new TestDatabaseInitializer();
		initializer.initializeLabResult();

		client = ModelUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
		
	
		IFindingsFactory iFindingsFactory = AllTests.getFindingsService().getFindingsFactory();
		IObservation persAnam = iFindingsFactory.createObservation();
		persAnam.setCategory(ObservationCategory.SOCIALHISTORY);
		persAnam.setCoding(
			Collections.singletonList(new TransientCoding(ObservationCode.ANAM_PERSONAL)));
		persAnam.setText("Pers Anamnese 1");
		persAnam.setPatientId(TestDatabaseInitializer.getPatient().getId());
		AllTests.getFindingsService().saveFinding(persAnam);
		
		IObservation risk = iFindingsFactory.createObservation();
		risk.setCategory(ObservationCategory.SOCIALHISTORY);
		risk.setCoding(
			Collections.singletonList(new TransientCoding(ObservationCode.ANAM_RISK)));
		risk.setText("Risiken 1");
		risk.setPatientId(TestDatabaseInitializer.getPatient().getId());
		AllTests.getFindingsService().saveFinding(risk);
	}

	@Test
	public void getObservation() {
		List<LabResult> labResults = TestDatabaseInitializer.getLabResults();
		
		Observation readObservation = client.read().resource(Observation.class)
				.withId(labResults.get(0).getId())
				.execute();
		assertNotNull(readObservation);

		// search by patient and category
		Bundle results = client.search().forResource(Observation.class)
				.where(Observation.SUBJECT.hasId(TestDatabaseInitializer.getPatient().getId()))
				.and(Condition.CATEGORY.exactly().code("laboratory"))
				.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		Observation observation = (Observation) entries.get(0).getResource();
		assertEquals(observation.getIdElement().getIdPart(), labResults.get(0).getId());

		results = client.search().forResource(Observation.class)
				.where(Observation.SUBJECT.hasId(TestDatabaseInitializer.getPatient().getId()))
				.and(Condition.CATEGORY.exactly().code("abc"))
				.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		entries = results.getEntry();
		assertTrue(entries.isEmpty());

		// search with date parameter
		results = client.search().forResource(Observation.class)
				.where(Observation.SUBJECT.hasId(TestDatabaseInitializer.getPatient().getId()))
				.and(Observation.DATE.exactly()
						.day(AllTests.getDate(LocalDateTime.of(2016, Month.DECEMBER, 14, 17, 44, 25))))
				.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		entries = results.getEntry();
		assertFalse(entries.isEmpty());

		results = client.search().forResource(Observation.class)
				.where(Observation.SUBJECT.hasId(TestDatabaseInitializer.getPatient().getId()))
				.and(Observation.DATE.exactly()
						.day(AllTests.getDate(LocalDateTime.of(2016, Month.DECEMBER, 1, 0, 0, 0))))
				.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		entries = results.getEntry();
		assertTrue(entries.isEmpty());

		// search with date parameter and code
		results = client.search().forResource(Observation.class)
				.where(Observation.SUBJECT.hasId(TestDatabaseInitializer.getPatient().getId()))
				.and(Observation.CODE.exactly()
						.systemAndCode(CodingSystem.ELEXIS_LOCAL_LABORATORY_VITOLABKEY.getSystem(), "2"))
				.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		entries = results.getEntry();
		assertFalse(entries.isEmpty());
		
		// search for pers anamnesis
		results =
			client.search().forResource(Observation.class)
				.where(Observation.SUBJECT.hasId(TestDatabaseInitializer.getPatient().getId()))
				.and(Observation.CODE.exactly()
					.systemAndCode(ObservationCode.ANAM_PERSONAL.getIdentifierSystem().getSystem(),
						ObservationCode.ANAM_PERSONAL.getCode()))
				.and(Observation.CATEGORY.exactly()
					.code(ObservationCategory.SOCIALHISTORY.getCode()))
				.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		entries = results.getEntry();
		assertFalse(entries.isEmpty());
		assertTrue(((Observation) entries.get(0).getResource()).getText().getDivAsString()
			.contains("Pers Anamnese 1"));
		
		// search for risk factors
		results = client.search().forResource(Observation.class)
			.where(Observation.SUBJECT.hasId(TestDatabaseInitializer.getPatient().getId()))
			.and(Observation.CODE.exactly().systemAndCode(
				ObservationCode.ANAM_RISK.getIdentifierSystem().getSystem(),
				ObservationCode.ANAM_RISK.getCode()))
			.and(Observation.CATEGORY.exactly().code(ObservationCategory.SOCIALHISTORY.getCode()))
			.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		entries = results.getEntry();
		assertFalse(entries.isEmpty());
		assertTrue(((Observation) entries.get(0).getResource()).getText().getDivAsString()
			.contains("Risiken 1"));
	}

	@Test
	public void lobaratoryObservations() throws FHIRException {
		// search by patient and category
		Bundle results = client.search().forResource(Observation.class)
				.where(Observation.SUBJECT.hasId(TestDatabaseInitializer.getPatient().getId()))
				.and(Condition.CATEGORY.exactly().code("laboratory")).returnBundle(Bundle.class).execute();
		assertNotNull(results);
		assertFalse(results.getEntry().isEmpty());
		@SuppressWarnings("unchecked")
		List<Observation> observations = (List<Observation>) ((List<?>) results.getEntry().parallelStream()
				.map(be -> be.getResource()).collect(Collectors.toList()));
		for (Observation observation : observations) {
			assertTrue(observation.hasEffectiveDateTimeType());
			assertTrue(observation.hasValue());
			assertNotNull(observation.getCode());
			List<Coding> coding = observation.getCode().getCoding();
			assertFalse(coding.isEmpty());
			for (Coding code : coding) {
				if (code.getCode().contains("NUMERIC")) {
					if (observation.hasValueQuantity()) {
						Quantity quantityValue = observation.getValueQuantity();
						assertNotNull(quantityValue);
					} else if (observation.hasValueStringType()) {
						StringType stringValue = observation.getValueStringType();
						assertNotNull(stringValue);
						assertTrue(Character.isDigit(stringValue.toString().charAt(0)));
					} else {
						fail("Unexpected vaue type" + observation.getValue());
					}
					List<ObservationReferenceRangeComponent> refs = observation.getReferenceRange();
					assertFalse(refs.isEmpty());
				} else if (code.getCode().contains("TEXT")) {
					StringType stringValue = observation.getValueStringType();
					assertNotNull(stringValue);
				}
			}
		}
	}
}
