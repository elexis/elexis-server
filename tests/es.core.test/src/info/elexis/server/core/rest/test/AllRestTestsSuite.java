package info.elexis.server.core.rest.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ UnauthenticatedTests.class, AdminAuthenticatedTests.class })
public class AllRestTestsSuite {
}
