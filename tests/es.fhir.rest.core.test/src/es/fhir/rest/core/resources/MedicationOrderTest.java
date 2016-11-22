package es.fhir.rest.core.resources;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.MedicationOrder;
import org.hl7.fhir.dstu3.model.MedicationOrder.MedicationOrderStatus;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import ch.elexis.core.findings.util.ModelUtil;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;

public class MedicationOrderTest {

	private static IGenericClient client;

	private static Patient patient;

	@BeforeClass
	public static void setupClass() {
		TestDatabaseInitializer initializer = new TestDatabaseInitializer();
		initializer.initializePrescription();

		client = ModelUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
		patient = client.read().resource(Patient.class).withId(TestDatabaseInitializer.getPatient().getId())
				.execute();
		assertNotNull(patient);
	}

	@Test
	public void getMedicationOrder() {
		// test with full id url
		Bundle results = client.search().forResource(MedicationOrder.class)
				.where(MedicationOrder.PATIENT.hasId(patient.getId())).returnBundle(Bundle.class)
				.execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		MedicationOrder order = (MedicationOrder) entries.get(0).getResource();
		// read
		MedicationOrder readOrder = client.read().resource(MedicationOrder.class).withId(order.getId()).execute();
		assertNotNull(readOrder);
		assertEquals(order.getId(), readOrder.getId());
		// test with id part only
		results = client.search().forResource(MedicationOrder.class)
				.where(MedicationOrder.PATIENT.hasId(patient.getIdElement().getIdPart())).returnBundle(Bundle.class)
				.execute();
		assertNotNull(results);
		entries = results.getEntry();
		assertFalse(entries.isEmpty());
		MedicationOrder foundOrder = (MedicationOrder) entries.get(0).getResource();
		assertEquals(order.getId(), foundOrder.getId());
	}

	@Test
	public void updateMedicationOrder() {
		// load existing order
		Bundle results = client.search().forResource(MedicationOrder.class)
				.where(MedicationOrder.PATIENT.hasId(patient.getId())).returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		Optional<MedicationOrder> activeOrder = getActiveOrderWithDosage(entries);
		assertTrue(activeOrder.isPresent());
		MedicationOrder updateOrder = activeOrder.get();
		updateOrder.getDosageInstruction().get(0).setText("test");

		MethodOutcome outcome = client.update().resource(updateOrder).execute();
		// read and validate change
		MedicationOrder oldOrder = client.read().resource(MedicationOrder.class).withId(activeOrder.get().getId())
				.execute();
		assertNotNull(oldOrder);
		MedicationOrder newOrder = client.read().resource(MedicationOrder.class).withId(outcome.getId()).execute();
		assertNotNull(newOrder);
		assertEquals(MedicationOrderStatus.COMPLETED, oldOrder.getStatus());
		assertEquals(MedicationOrderStatus.ACTIVE, newOrder.getStatus());
		assertEquals("test", newOrder.getDosageInstruction().get(0).getText());
	}

	private Optional<MedicationOrder> getActiveOrderWithDosage(List<BundleEntryComponent> orders) {
		for (BundleEntryComponent bundleEntryComponent : orders) {
			if (bundleEntryComponent.getResource() instanceof MedicationOrder) {
				MedicationOrder order = (MedicationOrder) bundleEntryComponent.getResource();
				if (order.getStatus() == MedicationOrderStatus.ACTIVE && !order.getDosageInstruction().isEmpty()) {
					return Optional.of(order);
				}
			}
		}
		return Optional.empty();
	}
}
