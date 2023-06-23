package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.types.Gender;
import info.elexis.server.fhir.rest.core.test.FhirUtil;

public class PlainResourceProviderTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		client = FhirUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
	}

	@Test
	public void performCombinedQuery() {
		IPerson contact1 = new IContactBuilder.PersonBuilder(CoreModelServiceHolder.get(), "Smith", "Agent",
				LocalDate.now(), Gender.MALE).buildAndSave();
		IOrganization contact2 = new IContactBuilder.OrganizationBuilder(CoreModelServiceHolder.get(), "Smith Company")
				.buildAndSave();
		IPatient contact3 = new IContactBuilder.PatientBuilder(CoreModelServiceHolder.get(), "Polina", "Smith",
				LocalDate.now().minusYears(14), Gender.FEMALE).buildAndSave();

		Bundle results = client.search().forAllResources()
				.where(new StringClientParam("_type").matches().values("Patient", "Organization"))
				.where(new StringClientParam("_filter").matches().value("name co Smith")).returnBundle(Bundle.class)
				.execute();
		assertEquals(3, results.getEntry().size());

		CoreModelServiceHolder.get().remove(contact1);
		CoreModelServiceHolder.get().remove(contact2);
		CoreModelServiceHolder.get().remove(contact3);
	}

}
