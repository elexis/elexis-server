package info.elexis.server.core.connector.elexis.services;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import info.elexis.server.core.connector.elexis.billable.BillingTest;

@RunWith(Suite.class)
@SuiteClasses({ PrescriptionServiceTest.class, KontaktServiceTest.class, LockServiceTest.class,
		ArtikelstammItemServiceTest.class, BillingTest.class })
public class AllTests {
}
