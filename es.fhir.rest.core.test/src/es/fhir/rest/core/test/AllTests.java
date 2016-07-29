package es.fhir.rest.core.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import es.fhir.rest.core.resources.MedicationOrderTest;
import es.fhir.rest.core.resources.PatientTest;

@RunWith(Suite.class)
@SuiteClasses({ MedicationOrderTest.class, PatientTest.class })
public class AllTests {

}
