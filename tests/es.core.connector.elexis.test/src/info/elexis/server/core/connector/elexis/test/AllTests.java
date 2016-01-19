package info.elexis.server.core.connector.elexis.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ JPAQueryTest.class, UserServiceTest.class, ArtikelstammItemServiceTest.class, KontaktServiceTest.class,
		PrescriptionTest.class })
public class AllTests {
}
