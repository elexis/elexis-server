package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.r4.model.Organization;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ch.elexis.core.test.initializer.TestDatabaseInitializer;
import info.elexis.server.fhir.rest.core.test.AllTests;
import info.elexis.server.fhir.rest.core.test.FhirUtil;

public class OrganizationTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		AllTests.getTestDatabaseInitializer().initializeOrganization();

		client = FhirUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
	}

	@Test
	public void getOrganization() {
		// search by name
		Bundle results = client.search().forResource(Organization.class)
				.where(Organization.NAME.matches().value("Test")).returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		Organization organization = (Organization) entries.get(0).getResource();
		// read with by id
		Organization readOrganization = client.read().resource(Organization.class).withId(organization.getId())
				.execute();
		assertNotNull(readOrganization);
		assertEquals(organization.getId(), readOrganization.getId());
	}

	/**
	 * Test all properties set by
	 * {@link TestDatabaseInitializer#initializeOrganization()}.
	 */
	@Test
	public void getOrganizationProperties() {
		Organization readOrganization = client.read().resource(Organization.class)
				.withId(TestDatabaseInitializer.getOrganization().getId())
				.execute();
		assertNotNull(readOrganization);

		assertEquals("Test Organization", readOrganization.getName());
		List<ContactPoint> telcoms = readOrganization.getTelecom();
		assertNotNull(telcoms);
		assertEquals(2, telcoms.size());
		assertEquals(1, telcoms.get(0).getRank());
		assertEquals("+01555345", telcoms.get(0).getValue());
		assertEquals(ContactPointUse.MOBILE, telcoms.get(1).getUse());
		assertEquals("+01444345", telcoms.get(1).getValue());
		List<Address> addresses = readOrganization.getAddress();
		assertNotNull(addresses);
		assertEquals(1, addresses.size());
		assertEquals("City", addresses.get(0).getCity());
		assertEquals("123", addresses.get(0).getPostalCode());
		assertEquals("Street 10", addresses.get(0).getLine().get(0).asStringValue());
	}
}
