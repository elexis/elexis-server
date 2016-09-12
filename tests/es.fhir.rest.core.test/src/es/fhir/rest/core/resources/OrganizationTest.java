package es.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Organization;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.IGenericClient;
import es.fhir.rest.core.test.FhirClient;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;

public class OrganizationTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() {
		TestDatabaseInitializer initializer = new TestDatabaseInitializer();
		initializer.initializeOrganization();

		client = FhirClient.getTestClient();
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
}
