package info.elexis.server.core.connector.elexis.billable;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ BillingTest.class, ICodeElementValuesTest.class, VerrechenbarTest.class })
public class AllBillingTests {

//	public static VerrechenbarTarmedLeistung getTarmedVerrechenbar(String tarmedCode) {
//		return new VerrechenbarTarmedLeistung(TarmedLeistungService.findFromCode(tarmedCode, new TimeTool()).get());
//	}
	
}
