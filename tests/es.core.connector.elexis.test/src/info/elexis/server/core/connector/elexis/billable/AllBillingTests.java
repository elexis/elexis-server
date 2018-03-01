package info.elexis.server.core.connector.elexis.billable;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.services.TarmedLeistungService;

@RunWith(Suite.class)
@SuiteClasses({ BillingTest.class, ICodeElementValuesTest.class, TarmedBillingTest.class, VerrechenbarTest.class,
		TarmedOptifierTest.class })
public class AllBillingTests {

	public static VerrechenbarTarmedLeistung getTarmedVerrechenbar(String tarmedCode) {
		return new VerrechenbarTarmedLeistung(TarmedLeistungService.findFromCode(tarmedCode, new TimeTool()).get());
	}
	
}
