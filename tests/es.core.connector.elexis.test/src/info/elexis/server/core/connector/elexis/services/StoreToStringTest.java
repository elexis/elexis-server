package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;
import info.elexis.server.core.service.StoreToStringService;

public class StoreToStringTest {

	private static BundleContext context;
	private static ServiceReference<StoreToStringService> serviceRef;

	@BeforeClass
	public static void beforeClass() {
		new TestDatabaseInitializer().initializePatient();

		context = FrameworkUtil.getBundle(KontaktService.class).getBundleContext();
		serviceRef = context.getServiceReference(StoreToStringService.class);
		assertNotNull(serviceRef);
	}

	@AfterClass
	public static void afterClass() {
		context.ungetService(serviceRef);
	}

	@Test
	public void storeToStringTest() {
		StoreToStringService service = context.getService(serviceRef);
		assertNotNull(service);

		Optional<String> patientString = service.storeToString(TestDatabaseInitializer.getPatient());
		assertTrue(patientString.isPresent());
		assertEquals("ch.elexis.data.Patient::" + TestDatabaseInitializer.getPatient().getId(), patientString.get());
	}

	@Test
	public void getFromStoreToStringTest() {
		StoreToStringService service = context.getService(serviceRef);
		assertNotNull(service);

		Optional<Object> patientObject = service
				.createFromString("ch.elexis.data.Patient::" + TestDatabaseInitializer.getPatient().getId());
		assertTrue(patientObject.isPresent());
	}
}
