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
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.HumanName.NameUse;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.junit.BeforeClass;
import org.junit.Test;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ch.elexis.core.constants.XidConstants;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.User;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;
import info.elexis.server.core.connector.elexis.services.UserService;
import info.elexis.server.fhir.rest.core.test.AllTests;

public class PractitionerRoleTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		TestDatabaseInitializer initializer = new TestDatabaseInitializer();
		initializer.initializeMandant();

		client = ModelUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
	}

	@Test
	public void getPractitionerRole() {
		// search by role
		Bundle results = client.search().forResource(PractitionerRole.class)
				.where(PractitionerRole.ROLE.exactly().systemAndCode(
						org.hl7.fhir.dstu3.model.codesystems.PractitionerRole.DOCTOR.getSystem(),
						org.hl7.fhir.dstu3.model.codesystems.PractitionerRole.DOCTOR.toCode()))
				.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		PractitionerRole practitionerRole = (PractitionerRole) entries.get(0).getResource();
		List<CodeableConcept> roles = practitionerRole.getCode();
		boolean doctorRoleFound = false;
		for (CodeableConcept role : roles) {
			List<Coding> codings = role.getCoding();
			for (Coding coding : codings) {
				if (coding.getSystem().equals(org.hl7.fhir.dstu3.model.codesystems.PractitionerRole.DOCTOR.getSystem())
						&& coding.getCode()
								.equals(org.hl7.fhir.dstu3.model.codesystems.PractitionerRole.DOCTOR.toCode())) {
					doctorRoleFound = true;
				}
			}
		}
		assertTrue(doctorRoleFound);
	}

	/**
	 * Test all properties set by
	 * {@link TestDatabaseInitializer#initializeMandant()}.
	 */
	@Test
	public void getPractitionerProperties() {
		Optional<User> user = UserService.findByKontakt(TestDatabaseInitializer.getMandant());
		assertTrue(user.isPresent());
		PractitionerRole readPractitionerRole = client.read().resource(PractitionerRole.class)
				.withId(user.get().getId()).execute();
		assertNotNull(readPractitionerRole);
		assertNotNull(readPractitionerRole.getPractitioner());

		Practitioner readPractitioner = client.read().resource(Practitioner.class)
				.withId(readPractitionerRole.getPractitioner().getReferenceElement().getIdPart()).execute();
		assertNotNull(readPractitioner);

		List<HumanName> names = readPractitioner.getName();
		assertNotNull(names);
		assertFalse(names.isEmpty());
		assertEquals(2, names.size());

		HumanName name = names.get(0);
		assertNotNull(name);
		assertEquals(NameUse.OFFICIAL, name.getUse());
		assertEquals("Mandant", name.getFamily());
		assertEquals("Test", name.getGivenAsSingleString());

		HumanName sysName = names.get(1);
		assertNotNull(sysName);
		assertEquals(NameUse.ANONYMOUS, sysName.getUse());
		assertEquals("tst", sysName.getText());

		Date dob = readPractitioner.getBirthDate();
		assertNotNull(dob);
		assertEquals(LocalDate.of(1970, Month.JANUARY, 1), AllTests.getLocalDateTime(dob).toLocalDate());
		assertEquals(AdministrativeGender.MALE, readPractitioner.getGender());
		List<ContactPoint> telcoms = readPractitioner.getTelecom();
		assertNotNull(telcoms);
		assertEquals(2, telcoms.size());
		assertEquals(ContactPointUse.HOME, telcoms.get(0).getUse());
		assertEquals("+01555234", telcoms.get(0).getValue());
		assertEquals(ContactPointUse.MOBILE, telcoms.get(1).getUse());
		assertEquals("+01444234", telcoms.get(1).getValue());
		List<Address> addresses = readPractitioner.getAddress();
		assertNotNull(addresses);
		assertEquals(1, addresses.size());
		assertEquals("City", addresses.get(0).getCity());
		assertEquals("123", addresses.get(0).getPostalCode());
		assertEquals("Street 100", addresses.get(0).getLine().get(0).asStringValue());

		List<Identifier> identifiers = readPractitioner.getIdentifier();
		boolean eanFound = false;
		boolean kskFound = false;
		for (Identifier identifier : identifiers) {
			if (identifier.getSystem().equals(XidConstants.DOMAIN_EAN)) {
				assertEquals("2000000000002", identifier.getValue());
				eanFound = true;
			}
			if (identifier.getSystem().equals("www.xid.ch/id/ksk")) {
				assertEquals("C000002", identifier.getValue());
				kskFound = true;
			}
		}
		assertTrue(eanFound);
		assertTrue(kskFound);
		assertTrue(readPractitioner.getActive());
	}
}
