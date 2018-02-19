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
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import es.fhir.rest.core.resources.AllergyIntoleranceTest;
import es.fhir.rest.core.resources.ClaimTest;
import es.fhir.rest.core.resources.CodesySystemTest;
import es.fhir.rest.core.resources.ConditionTest;
import es.fhir.rest.core.resources.CoverageTest;
import es.fhir.rest.core.resources.EncounterTest;
import es.fhir.rest.core.resources.FamilyMemberHistoryTest;
import es.fhir.rest.core.resources.MedicationRequestTest;
import es.fhir.rest.core.resources.ObservationTest;
import es.fhir.rest.core.resources.OrganizationTest;
import es.fhir.rest.core.resources.PatientTest;
import es.fhir.rest.core.resources.PractitionerRoleTest;
import es.fhir.rest.core.resources.ProcedureRequestTest;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;

@RunWith(Suite.class)
@SuiteClasses({
	MedicationRequestTest.class, PatientTest.class, OrganizationTest.class, CoverageTest.class,
	PractitionerRoleTest.class, EncounterTest.class, ConditionTest.class, CodesySystemTest.class,
	ProcedureRequestTest.class, ClaimTest.class, ObservationTest.class,
	FamilyMemberHistoryTest.class, AllergyIntoleranceTest.class
})
public class AllTests {
	
	private static IFindingsService iFindingsService;
	
	private static TestDatabaseInitializer testDatabaseInitializer = new TestDatabaseInitializer();

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
	
	public static IFindingsService getFindingsService(){
		if (iFindingsService != null) {
			return iFindingsService;
		}
		BundleContext bundleContext = FrameworkUtil.getBundle(AllTests.class).getBundleContext();
		ServiceReference<IFindingsService> serviceReference =
			bundleContext.getServiceReference(IFindingsService.class);
		if (serviceReference != null) {
			iFindingsService = bundleContext.getService(serviceReference);
		}
		return iFindingsService;
	}
	
	public static void deleteAllFindings(){
		IFindingsService iFindingsService = getFindingsService();
		if (iFindingsService != null) {
			for (IFinding iFinding : iFindingsService.getPatientsFindings(
				testDatabaseInitializer.getPatient().getId(), IFinding.class)) {
				iFindingsService.deleteFinding(iFinding);
			}
		}
	}
	
	public static TestDatabaseInitializer getTestDatabaseInitializer() {
		return testDatabaseInitializer;
	}
}
