package es.fhir.rest.core.test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

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

	public static Date getDate(LocalDateTime localDateTime) {
		ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
		return Date.from(zdt.toInstant());
	}

	public static LocalDateTime getLocalDateTime(Date date) {
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}
}
