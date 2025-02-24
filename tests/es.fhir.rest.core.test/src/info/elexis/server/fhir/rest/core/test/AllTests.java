package info.elexis.server.fhir.rest.core.test;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IElexisEntityManager;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.test.initializer.TestDatabaseInitializer;
import ch.elexis.core.utils.OsgiServiceUtil;
import info.elexis.server.fhir.rest.core.resources.AllResourceTests;
import info.elexis.server.fhir.rest.core.resources.util.IContactSearchFilterQueryAdapterTest;

@RunWith(Suite.class)
@SuiteClasses({ AllResourceTests.class, IContactSearchFilterQueryAdapterTest.class })
public class AllTests {

	private static IFindingsService iFindingsService;

	public static final String GENERIC_CLIENT_URL = "http://localhost:8380/fhir";

	private static IModelService modelService;
	private static IElexisEntityManager entityManager;
	private static IConfigService configService;

	private static TestDatabaseInitializer testDatabaseInitializer;

	@BeforeClass
	public static void beforeClass() throws IOException, SQLException {
		modelService = OsgiServiceUtil
				.getService(IModelService.class, "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();
		entityManager = OsgiServiceUtil.getService(IElexisEntityManager.class).get();
		configService = OsgiServiceUtil.getService(IConfigService.class).get();

		testDatabaseInitializer = new TestDatabaseInitializer(modelService, entityManager);

		testDatabaseInitializer.setConfigService(configService);
		testDatabaseInitializer.initializeDb();
		testDatabaseInitializer.initializeMandant();
	}

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

	public static IFindingsService getFindingsService() {
		if (iFindingsService == null) {
			iFindingsService = OsgiServiceUtil.getServiceWait(IFindingsService.class, 5000).orElseThrow();
		}
		return iFindingsService;
	}

	public static void deleteAllFindings() {
		IFindingsService iFindingsService = getFindingsService();
		if (iFindingsService != null) {
			for (IFinding iFinding : iFindingsService.getPatientsFindings(testDatabaseInitializer.getPatient().getId(),
					IFinding.class)) {
				iFindingsService.deleteFinding(iFinding);
			}
		}
	}

	public static TestDatabaseInitializer getTestDatabaseInitializer() {
		return testDatabaseInitializer;
	}

	public static IModelService getModelService() {
		return modelService;
	}
}
