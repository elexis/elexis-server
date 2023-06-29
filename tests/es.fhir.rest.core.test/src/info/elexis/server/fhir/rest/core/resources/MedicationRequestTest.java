package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Dosage.DosageDoseAndRateComponent;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestDispenseRequestComponent;
import org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestIntent;
import org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestPriority;
import org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestStatus;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.SimpleQuantity;
import org.hl7.fhir.r4.model.Timing;
import org.hl7.fhir.r4.model.Timing.EventTiming;
import org.hl7.fhir.r4.model.Timing.TimingRepeatComponent;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ch.elexis.core.findings.util.fhir.MedicamentCoding;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.test.initializer.TestDatabaseInitializer;
import info.elexis.server.fhir.rest.core.test.AllTests;
import info.elexis.server.fhir.rest.core.test.FhirUtil;

public class MedicationRequestTest {

	private static IGenericClient client;

	private static Patient patient;

	private static Practitioner practitioner;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		AllTests.getTestDatabaseInitializer().initializePrescription();

		client = FhirUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
		patient = client.read().resource(Patient.class)
				.withId(AllTests.getTestDatabaseInitializer().getPatient().getId()).execute();
		assertNotNull(patient);

		AllTests.getTestDatabaseInitializer();
		practitioner = client.read().resource(Practitioner.class).withId(TestDatabaseInitializer.getMandant().getId())
				.execute();
		assertNotNull(practitioner);
	}

	@Test
	public void getMedicationRequest() {
		// test with full id url
		Bundle results = client.search().forResource(MedicationRequest.class)
				.where(MedicationRequest.PATIENT.hasId(patient.getId())).returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		MedicationRequest order = (MedicationRequest) entries.get(0).getResource();
		// read
		MedicationRequest readRequest = client.read().resource(MedicationRequest.class)
				.withId(order.getIdElement().getIdPart()).execute();
		assertNotNull(readRequest);
		assertEquals(order.getId(), readRequest.getId());
		// test with id part only
		results = client.search().forResource(MedicationRequest.class)
				.where(MedicationRequest.PATIENT.hasId(patient.getIdElement().getIdPart())).returnBundle(Bundle.class)
				.execute();
		assertNotNull(results);
		entries = results.getEntry();
		assertFalse(entries.isEmpty());
		MedicationRequest foundRequest = (MedicationRequest) entries.get(0).getResource();
		assertEquals(order.getId(), foundRequest.getId());
	}

	@Test
	public void createMedicationRequest() {
		// test with full id url
		Bundle results = client.search().forResource(MedicationRequest.class)
				.where(MedicationRequest.PATIENT.hasId(patient.getId())).returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		int beforeCreate = entries.size();

		MedicationRequest medicationRequest = new MedicationRequest();
		medicationRequest.setSubject(new Reference(patient));
		medicationRequest.setRecorder(new Reference(practitioner));
		IArticle article = AllTests.getTestDatabaseInitializer().getArticle();
		CodeableConcept medication = new CodeableConcept();
		Coding coding = medication.addCoding();
		coding.setSystem(MedicamentCoding.GTIN.getOid());
		coding.setCode(article.getGtin());
		coding.setDisplay(article.getName());
		medicationRequest.setMedication(medication);
		MedicationRequestDispenseRequestComponent dispenseRequest = new MedicationRequestDispenseRequestComponent();
		Period dispensePeriod = new Period();
		LocalDateTime dateFrom = LocalDateTime.now().minusDays(7);
		if (dateFrom != null) {
			Date time = Date.from(dateFrom.atZone(ZoneId.systemDefault()).toInstant());
			dispensePeriod.setStart(time);
		}
		dispenseRequest.setValidityPeriod(dispensePeriod);
		medicationRequest.setDispenseRequest(dispenseRequest);

		Dosage dosage = medicationRequest.addDosageInstruction();
		dosage.setText("1-0-1-0");
		medicationRequest.setStatus(MedicationRequestStatus.ACTIVE);
		medicationRequest.setIntent(MedicationRequestIntent.ORDER);
		medicationRequest.setPriority(MedicationRequestPriority.ROUTINE);
		
		MethodOutcome outcome = client.create().resource(medicationRequest).execute();
		assertTrue(outcome.getCreated());
		assertNotNull(outcome.getResource());

		MedicationRequest createdOrder = client.read().resource(MedicationRequest.class)
				.withId(outcome.getId().getIdPart()).execute();
		assertNotNull(createdOrder);
		assertEquals(MedicationRequestStatus.ACTIVE, createdOrder.getStatus());
		assertEquals(medicationRequest.getDispenseRequest().getValidityPeriod().getStart().toString(),
				createdOrder.getDispenseRequest().getValidityPeriod().getStart().toString());
		assertEquals(medicationRequest.getDosageInstructionFirstRep().getText(),
				createdOrder.getDosageInstructionFirstRep().getText());

		results = client.search().forResource(MedicationRequest.class)
				.where(MedicationRequest.PATIENT.hasId(patient.getId())).returnBundle(Bundle.class).execute();
		assertNotNull(results);
		entries = results.getEntry();
		assertTrue(beforeCreate < entries.size());
	}

	@Test
	public void createMedicationRequestDosageDoseAndRate() {
		MedicationRequest medicationRequest = new MedicationRequest();
		medicationRequest.setSubject(new Reference(patient));
		medicationRequest.setRecorder(new Reference(practitioner));
		IArticle article = AllTests.getTestDatabaseInitializer().getArticle();
		CodeableConcept medication = new CodeableConcept();
		Coding coding = medication.addCoding();
		coding.setSystem(MedicamentCoding.GTIN.getOid());
		coding.setCode(article.getGtin());
		coding.setDisplay(article.getName());
		medicationRequest.setMedication(medication);
		MedicationRequestDispenseRequestComponent dispenseRequest = new MedicationRequestDispenseRequestComponent();
		Period dispensePeriod = new Period();
		LocalDateTime dateFrom = LocalDateTime.now().minusDays(7);
		if (dateFrom != null) {
			Date time = Date.from(dateFrom.atZone(ZoneId.systemDefault()).toInstant());
			dispensePeriod.setStart(time);
		}
		dispenseRequest.setValidityPeriod(dispensePeriod);
		medicationRequest.setDispenseRequest(dispenseRequest);

		Dosage dosage = medicationRequest.addDosageInstruction();
		Timing timing = new Timing();
		TimingRepeatComponent repeat = new TimingRepeatComponent();
		repeat.addWhen(EventTiming.MORN);
		timing.setRepeat(repeat);
		dosage.setTiming(timing);
		DosageDoseAndRateComponent doseAndRate = new DosageDoseAndRateComponent();
		doseAndRate.setDose(new SimpleQuantity().setValue(1.0));
		dosage.addDoseAndRate(doseAndRate);

		dosage = medicationRequest.addDosageInstruction();
		timing = new Timing();
		repeat = new TimingRepeatComponent();
		repeat.addWhen(EventTiming.AFT);
		timing.setRepeat(repeat);
		dosage.setTiming(timing);
		doseAndRate = new DosageDoseAndRateComponent();
		doseAndRate.setDose(new SimpleQuantity().setValue(1.0));
		dosage.addDoseAndRate(doseAndRate);

		medicationRequest.setStatus(MedicationRequestStatus.ACTIVE);
		medicationRequest.setIntent(MedicationRequestIntent.PLAN);
		medicationRequest.setPriority(MedicationRequestPriority.ROUTINE);
		
		MethodOutcome outcome = client.create().resource(medicationRequest).execute();
		assertTrue(outcome.getCreated());
		assertNotNull(outcome.getResource());

		MedicationRequest createdOrder = client.read().resource(MedicationRequest.class)
				.withId(outcome.getId().getIdPart())
				.execute();
		assertNotNull(createdOrder);
		assertEquals(MedicationRequestStatus.ACTIVE, createdOrder.getStatus());
		assertEquals(medicationRequest.getDispenseRequest().getValidityPeriod().getStart().toString(),
				createdOrder.getDispenseRequest().getValidityPeriod().getStart().toString());
		assertEquals("1-0-1-0", createdOrder.getDosageInstructionFirstRep().getText());
	}
	
	@Test
	public void createMedicationRequestOrder() {
		MedicationRequest medicationRequest = new MedicationRequest();
		medicationRequest.setSubject(new Reference(patient));
		medicationRequest.setRecorder(new Reference(practitioner));
		IArticle article = AllTests.getTestDatabaseInitializer().getArticle();
		CodeableConcept medication = new CodeableConcept();
		Coding coding = medication.addCoding();
		coding.setSystem(MedicamentCoding.GTIN.getOid());
		coding.setCode(article.getGtin());
		coding.setDisplay(article.getName());
		medicationRequest.setMedication(medication);
		MedicationRequestDispenseRequestComponent dispenseRequest = new MedicationRequestDispenseRequestComponent();
		Period dispensePeriod = new Period();
		LocalDateTime dateFrom = LocalDateTime.now().minusDays(7);
		if (dateFrom != null) {
			Date time = Date.from(dateFrom.atZone(ZoneId.systemDefault()).toInstant());
			dispensePeriod.setStart(time);
		}
		dispenseRequest.setValidityPeriod(dispensePeriod);
		medicationRequest.setDispenseRequest(dispenseRequest);

		Dosage dosage = medicationRequest.addDosageInstruction();
		Timing timing = new Timing();
		TimingRepeatComponent repeat = new TimingRepeatComponent();
		repeat.addWhen(EventTiming.MORN);
		timing.setRepeat(repeat);
		dosage.setTiming(timing);
		DosageDoseAndRateComponent doseAndRate = new DosageDoseAndRateComponent();
		doseAndRate.setDose(new SimpleQuantity().setValue(1.0));
		dosage.addDoseAndRate(doseAndRate);

		dosage = medicationRequest.addDosageInstruction();
		timing = new Timing();
		repeat = new TimingRepeatComponent();
		repeat.addWhen(EventTiming.AFT);
		timing.setRepeat(repeat);
		dosage.setTiming(timing);
		doseAndRate = new DosageDoseAndRateComponent();
		doseAndRate.setDose(new SimpleQuantity().setValue(1.0));
		dosage.addDoseAndRate(doseAndRate);

		medicationRequest.setStatus(MedicationRequestStatus.ACTIVE);
		medicationRequest.setIntent(MedicationRequestIntent.ORDER);
		medicationRequest.setPriority(MedicationRequestPriority.ROUTINE);

		MethodOutcome outcome = client.create().resource(medicationRequest).execute();
		assertTrue(outcome.getCreated());
		assertNotNull(outcome.getResource());

		// created recipe is reference as group identifier
		MedicationRequest createdOrder = client.read().resource(MedicationRequest.class)
				.withId(outcome.getId().getIdPart()).execute();
		assertNotNull(createdOrder);
		assertEquals(MedicationRequestStatus.ACTIVE, createdOrder.getStatus());
		assertEquals("1-0-1-0", createdOrder.getDosageInstructionFirstRep().getText());
		assertTrue(StringUtils.isNotBlank(createdOrder.getGroupIdentifier().getValue()));

		// add same request to the recipe
		medicationRequest.setGroupIdentifier(createdOrder.getGroupIdentifier());
		outcome = client.create().resource(medicationRequest).execute();
		assertTrue(outcome.getCreated());
		assertNotNull(outcome.getResource());
		assertNotEquals(createdOrder.getIdElement().getIdPart(),
				((MedicationRequest) outcome.getResource()).getIdElement().getIdPart());
		assertEquals(createdOrder.getGroupIdentifier().getValue(),
				((MedicationRequest) outcome.getResource()).getGroupIdentifier().getValue());
	}

	@Test
	public void updateMedicationRequest() {
		// load existing order
		Bundle results = client.search().forResource(MedicationRequest.class)
				.where(MedicationRequest.PATIENT.hasId(patient.getId())).returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		Optional<MedicationRequest> activeOrder = getActiveRequestWithDosage(entries);
		assertTrue(activeOrder.isPresent());
		MedicationRequest updateOrder = activeOrder.get();
		updateOrder.getDosageInstruction().get(0).setText("test");
		List<Extension> entryTypes = updateOrder
				.getExtensionsByUrl("www.elexis.info/extensions/prescription/entrytype");
		assertEquals(EntryType.FIXED_MEDICATION.name(), ((CodeType) entryTypes.get(0).getValue()).getValue());
		entryTypes.get(0).setValue(new CodeType(EntryType.SYMPTOMATIC_MEDICATION.name()));

		// update the medication
		MethodOutcome outcome = client.update().resource(updateOrder).execute();

		// read and validate change
		MedicationRequest oldOrder = client.read().resource(MedicationRequest.class)
				.withId(activeOrder.get().getIdElement().getIdPart()).execute();
		assertNotNull(oldOrder);
		MedicationRequest newOrder = client.read().resource(MedicationRequest.class).withId(outcome.getId()).execute();
		assertNotNull(newOrder);
		assertEquals(MedicationRequestStatus.STOPPED, oldOrder.getStatus());
		assertEquals(MedicationRequestStatus.ACTIVE, newOrder.getStatus());
		assertEquals("test", newOrder.getDosageInstruction().get(0).getText());
		entryTypes = newOrder.getExtensionsByUrl("www.elexis.info/extensions/prescription/entrytype");
		assertEquals(EntryType.SYMPTOMATIC_MEDICATION.name(), ((CodeType) entryTypes.get(0).getValue()).getValue());
	}

	@Test
	public void stopMedicationRequest() {
		// load existing order and stop
		Bundle results = client.search().forResource(MedicationRequest.class)
				.where(MedicationRequest.PATIENT.hasId(patient.getId())).returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		Optional<MedicationRequest> activeRequest = getActiveRequestWithDosage(entries);
		assertTrue(activeRequest.isPresent());
		MedicationRequest updateRequest = activeRequest.get();
		updateRequest.setStatus(MedicationRequestStatus.STOPPED);
		updateRequest.getDispenseRequest().getValidityPeriod().setEnd(new Date());

		// update the medication
		MethodOutcome outcome = client.update().resource(updateRequest).execute();

		// read and validate change
		assertEquals(activeRequest.get().getIdElement().getIdPart(), outcome.getId().getIdPart());
		MedicationRequest updatedRequest = client.read().resource(MedicationRequest.class).withId(outcome.getId())
				.execute();
		assertNotNull(updatedRequest);
		assertEquals(MedicationRequestStatus.STOPPED, updatedRequest.getStatus());
	}

	private Optional<MedicationRequest> getActiveRequestWithDosage(List<BundleEntryComponent> orders) {
		for (BundleEntryComponent bundleEntryComponent : orders) {
			if (bundleEntryComponent.getResource() instanceof MedicationRequest) {
				MedicationRequest order = (MedicationRequest) bundleEntryComponent.getResource();
				if (order.getStatus() == MedicationRequestStatus.ACTIVE && !order.getDosageInstruction().isEmpty()) {
					return Optional.of(order);
				}
			}
		}
		return Optional.empty();
	}
}
