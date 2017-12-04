package info.elexis.server.core.connector.elexis.billable;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ BillingTest.class, ICodeElementValuesTest.class, TarmedBillingTest.class, VerrechenbarTest.class,
		TarmedOptifierTest.class })
public class AllBillingTests {

}
