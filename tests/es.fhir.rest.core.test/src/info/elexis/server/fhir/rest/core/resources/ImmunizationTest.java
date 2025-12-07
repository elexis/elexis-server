package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Reference;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ch.elexis.core.findings.util.fhir.MedicamentCoding;
import ch.elexis.core.model.IVaccination;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.builder.IVaccinationBuilder;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import info.elexis.server.fhir.rest.core.test.AllTests;
import info.elexis.server.fhir.rest.core.test.FhirUtil;

public class ImmunizationTest {
	
	private static IGenericClient client;
	
	private IVaccination articleVaccination;

	private IVaccination customVaccination;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException{
		AllTests.getTestDatabaseInitializer().initializePatient();
		AllTests.getTestDatabaseInitializer().initializePrescription();
		
		
		client = FhirUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
		
	}

	@Before
	public void before() {
		articleVaccination = new IVaccinationBuilder(CoreModelServiceHolder.get(), null,
				AllTests.getTestDatabaseInitializer().getArticle(), AllTests.getTestDatabaseInitializer().getPatient())
				.build();
		articleVaccination.setDateOfAdministration(LocalDate.of(1999, 12, 12));
		CoreModelServiceHolder.get().save(articleVaccination);

		customVaccination = new IVaccinationBuilder(CoreModelServiceHolder.get(), null,
				"test vaccine", "0123456789012", "J07BK03", AllTests.getTestDatabaseInitializer().getPatient())
				.build();
		customVaccination.setDateOfAdministration(LocalDate.of(2001, 1, 1));
		CoreModelServiceHolder.get().save(customVaccination);
	}

	@After
	public void after(){
		List<IVaccination> vaccinations = CoreModelServiceHolder.get().getQuery(IVaccination.class).and(ModelPackage.Literals.IVACCINATION__PATIENT,
				COMPARATOR.EQUALS, AllTests.getTestDatabaseInitializer().getPatient()).execute();
		vaccinations.forEach(v -> CoreModelServiceHolder.get().remove(v));
	}
	
	@Test
	public void findImmunization() {
		Bundle results = client.search().forResource(Immunization.class)
			.where(AllergyIntolerance.PATIENT.hasId(AllTests.getTestDatabaseInitializer().getPatient().getId()))
			.returnBundle(Bundle.class).execute();
		assertNotNull(results);
		List<BundleEntryComponent> entries = results.getEntry();
		assertFalse(entries.isEmpty());
		assertEquals(2, entries.size());
	}
	
	@Test
	public void createImmunization() {
		Immunization immunization = new Immunization();
		
		DateTimeType occurence = new DateTimeType("2012-02-02");
		immunization.setOccurrence(occurence);

		immunization.setPatient(new Reference("Patient/" + AllTests.getTestDatabaseInitializer().getPatient().getId()));
		
		Coding vaccineGtinCoding = immunization.getVaccineCode().addCoding();
		vaccineGtinCoding.setSystem(MedicamentCoding.GTIN.getOid());
		vaccineGtinCoding.setCode("0123456789013");
		vaccineGtinCoding.setDisplay("test vaccine other");
		Coding vaccineAtcCoding = immunization.getVaccineCode().addCoding();
		vaccineAtcCoding.setSystem(MedicamentCoding.ATC.getOid());
		vaccineAtcCoding.setCode("J07BK04");
		vaccineAtcCoding.setDisplay("test atc");

		MethodOutcome outcome = client.create().resource(immunization).execute();
		assertNotNull(outcome);
		assertTrue(outcome.getCreated());
		assertNotNull(outcome.getId());
		
		Immunization readImmunization = client.read().resource(Immunization.class).withId(outcome.getId().getIdPart())
				.execute();
		assertNotNull(readImmunization);
		assertEquals(outcome.getId().getIdPart(),
				readImmunization.getIdElement().getIdPart());

		assertEquals(occurence.getYear(), readImmunization.getOccurrenceDateTimeType().getYear());
		assertEquals(occurence.getMonth(), readImmunization.getOccurrenceDateTimeType().getMonth());
		assertEquals(occurence.getDay(), readImmunization.getOccurrenceDateTimeType().getDay());
	}
	
	@Test
	public void updateImmunization() {
		Immunization readImmunization = client.read().resource(Immunization.class).withId(customVaccination.getId())
				.execute();
		assertNotNull(readImmunization);

		DateTimeType updateOccurence = new DateTimeType("2002-02-02");
		readImmunization.setOccurrence(updateOccurence);

		// update the immunization
		MethodOutcome outcome = client.update().resource(readImmunization).execute();

		readImmunization = client.read().resource(Immunization.class).withId(outcome.getId().getIdPart()).execute();
		assertNotNull(readImmunization);

		assertEquals(customVaccination.getId(), readImmunization.getIdElement().getIdPart());
		assertEquals(outcome.getId().getIdPart(), readImmunization.getIdElement().getIdPart());

		assertEquals(updateOccurence.getYear(), readImmunization.getOccurrenceDateTimeType().getYear());
		assertEquals(updateOccurence.getMonth(), readImmunization.getOccurrenceDateTimeType().getMonth());
		assertEquals(updateOccurence.getDay(), readImmunization.getOccurrenceDateTimeType().getDay());
	}
}
