package es.fhir.rest.core.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import es.fhir.rest.core.resources.CoverageTest;
import es.fhir.rest.core.resources.MedicationOrderTest;
import es.fhir.rest.core.resources.OrganizationTest;
import es.fhir.rest.core.resources.PatientTest;
import es.fhir.rest.core.resources.PractitionerTest;

@RunWith(Suite.class)
@SuiteClasses({ MedicationOrderTest.class, PatientTest.class, OrganizationTest.class, CoverageTest.class,
		PractitionerTest.class })
public class AllTests {

}
