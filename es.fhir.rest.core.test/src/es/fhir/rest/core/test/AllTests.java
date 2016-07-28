package es.fhir.rest.core.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import es.fhir.rest.core.resources.MedicationOrderTest;

@RunWith(Suite.class)
@SuiteClasses({ MedicationOrderTest.class })
public class AllTests {

}
