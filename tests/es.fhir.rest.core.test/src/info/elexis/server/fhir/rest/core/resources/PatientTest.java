package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Date;
import java.util.List;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.IGenericClient;
import ch.elexis.core.constants.XidConstants;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;
import info.elexis.server.fhir.rest.core.test.AllTests;

public class PatientTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		TestDatabaseInitializer initializer = new TestDatabaseInitializer();
		initializer.initializePatient();

		client = ModelUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);

	}

	@Test
	public void getPatient() {
		// search by name
		Bundle results = client.search().forResource(Patient.class).where(Patient.NAME.matches().value("Test"))
				.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		Patient patient = (Patient) entries.get(0).getResource();
		// read with by id
		Patient readPatient = client.read().resource(Patient.class).withId(patient.getId()).execute();
		assertNotNull(readPatient);
		assertEquals(patient.getId(), readPatient.getId());
		// search by elexis patient number identifier
		results = client.search()
				.forResource(Patient.class).where(Patient.IDENTIFIER.exactly()
						.systemAndIdentifier("www.elexis.info/patnr",
				Integer.toString(getPatientNumber(patient)))).returnBundle(Bundle.class).execute();
		assertNotNull(results);
		entries = results.getEntry();
		assertFalse(entries.isEmpty());

	}

	/**
	 * Test all properties set by
	 * {@link TestDatabaseInitializer#initializePatient()}.
	 */
	@Test
	public void getPatientProperties() {
		Patient readPatient = client.read().resource(Patient.class).withId(AllTests.getTestDatabaseInitializer().getPatient().getId())
				.execute();
		assertNotNull(readPatient);
		List<HumanName> names = readPatient.getName();
		assertNotNull(names);
		assertFalse(names.isEmpty());
		HumanName name = names.get(0);
		assertNotNull(name);
		assertEquals("Patient", name.getFamily());
		assertEquals("Test", name.getGivenAsSingleString());
		Date dob = readPatient.getBirthDate();
		assertNotNull(dob);
		assertEquals(LocalDate.of(1990, Month.JANUARY, 1), AllTests.getLocalDateTime(dob).toLocalDate());
		assertEquals(AdministrativeGender.FEMALE, readPatient.getGender());
		List<ContactPoint> telcoms = readPatient.getTelecom();
		assertNotNull(telcoms);
		assertEquals(2, telcoms.size());
		assertEquals(ContactPointUse.HOME, telcoms.get(0).getUse());
		assertEquals("+01555123", telcoms.get(0).getValue());
		assertEquals(ContactPointUse.MOBILE, telcoms.get(1).getUse());
		assertEquals("+01444123", telcoms.get(1).getValue());
		List<Address> addresses = readPatient.getAddress();
		assertNotNull(addresses);
		assertEquals(1, addresses.size());
		assertEquals("City", addresses.get(0).getCity());
		assertEquals("123", addresses.get(0).getPostalCode());
		assertEquals("Street 1", addresses.get(0).getLine().get(0).asStringValue());

		List<Identifier> identifiers = readPatient.getIdentifier();
		boolean ahvFound = false;
		for (Identifier identifier : identifiers) {
			if (identifier.getSystem().equals(XidConstants.CH_AHV)) {
				assertTrue(identifier.getValue().startsWith("756"));
				ahvFound = true;
			}
		}
		assertTrue(ahvFound);
	}

	public int getPatientNumber(Patient patient) {
		List<Identifier> identifiers = patient.getIdentifier();
		for (Identifier identifier : identifiers) {
			if ("www.elexis.info/patnr".equals(identifier.getSystem())) {
				return Integer.parseInt(identifier.getValue());
			}
		}
		return -1;
	}
}
