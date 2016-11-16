package es.fhir.rest.core.test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import es.fhir.rest.core.resources.CodesySystemTest;
import es.fhir.rest.core.resources.ConditionTest;
import es.fhir.rest.core.resources.CoverageTest;
import es.fhir.rest.core.resources.EncounterTest;
import es.fhir.rest.core.resources.MedicationOrderTest;
import es.fhir.rest.core.resources.OrganizationTest;
import es.fhir.rest.core.resources.PatientTest;
import es.fhir.rest.core.resources.PractitionerTest;

@RunWith(Suite.class)
@SuiteClasses({ MedicationOrderTest.class, PatientTest.class, OrganizationTest.class, CoverageTest.class,
		PractitionerTest.class, EncounterTest.class, ConditionTest.class, CodesySystemTest.class })
public class AllTests {

	public static Date getDate(LocalDateTime localDateTime) {
		ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
		return Date.from(zdt.toInstant());
	}

	public static LocalDateTime getLocalDateTime(Date date) {
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	public static boolean isCodeInConcept(String system, String code, CodeableConcept concept) {
		List<Coding> list = concept.getCoding();
		if (list != null && !list.isEmpty()) {
			for (Coding coding : list) {
				if (coding.getSystem().equals(system) && coding.getCode().equals(code)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isCodeInConcepts(String system, String code, List<CodeableConcept> concepts) {
		if (concepts != null && !concepts.isEmpty()) {
			for (CodeableConcept concept : concepts) {
				if (isCodeInConcept(system, code, concept)) {
					return true;
				}
			}
		}
		return false;
	}
}
