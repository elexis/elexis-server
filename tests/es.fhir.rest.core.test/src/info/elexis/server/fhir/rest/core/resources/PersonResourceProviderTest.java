package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.HumanName.NameUse;
import org.hl7.fhir.r4.model.Person;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.types.Gender;
import info.elexis.server.fhir.rest.core.test.AllTests;
import info.elexis.server.fhir.rest.core.test.FhirUtil;

public class PersonResourceProviderTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		client = FhirUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
	}

	@Test
	public void createDelete() {
		Person person = new Person();
		HumanName hn = new HumanName();
		hn.setUse(NameUse.OFFICIAL);
		hn.setFamily("familyName");
		person.setName(Collections.singletonList(hn));
		person.setBirthDate(new Date());
		Address address = new Address();
		address.setCity("City");
		address.setCountry("CH");
		person.setAddress(Collections.singletonList(address));

		// create
		MethodOutcome execute = client.create().resource(person).execute();
		assertTrue(execute.getCreated());
		assertNotNull(execute.getId());
		assertEquals("Person", execute.getId().getResourceType());
		IIdType id = execute.getId();
		Person created = client.read().resource(Person.class).withId(id).execute();
		assertEquals(hn.getFamily(), created.getName().get(0).getFamily());

		// delete
		client.delete().resource(created).execute();
		Optional<IPerson> load = AllTests.getModelService().load(id.getIdPart(), IPerson.class, true);
		assertTrue(load.isPresent());
		assertTrue(load.get().isDeleted());
	}

	@Test(expected = ResourceNotFoundException.class)
	public void deleteNonExistentPatient() {
		client.delete().resourceById("Person", "doesNotExist").execute();
	}

	@Test
	public void getPatient() {
		// we re-use the patient as person
		Person readPatient = client.read().resource(Person.class)
				.withId(AllTests.getTestDatabaseInitializer().getPatient().getId()).execute();
		assertTrue(readPatient.getId().contains(AllTests.getTestDatabaseInitializer().getPatient().getId()));
	}

	@Test
	public void searchByNameOrAddress() {
		IPerson test1 = new IContactBuilder.PersonBuilder(CoreModelServiceHolder.get(), "MaloiFirstname",
				"MaloiLastname", LocalDate.now(), Gender.MALE).buildAndSave();
		IPerson test2 = new IContactBuilder.PersonBuilder(CoreModelServiceHolder.get(), "Firstname", "Lastname",
				LocalDate.now(), Gender.MALE).build();
		test2.setStreet("Maloistrasse 15");
		test2.setCity("Windwil");
		CoreModelServiceHolder.get().save(test2);

		Bundle results = client.search().byUrl("Person?_filter=name%20co%20%22aloi%22%20or%20address%20co%20%22aloi%22")
				.returnBundle(Bundle.class).execute();
		assertEquals(2, results.getEntry().size());
		String id0 = results.getEntry().get(0).getResource().getIdElement().getIdPart();
		String id1 = results.getEntry().get(1).getResource().getIdElement().getIdPart();
		assertTrue(id0.equals(test1.getId()) || id1.equals(test2.getId()));
		assertTrue(id0.equals(test1.getId()) || id1.equals(test2.getId()));

		CoreModelServiceHolder.get().remove(test1);
		CoreModelServiceHolder.get().remove(test2);
	}

}
