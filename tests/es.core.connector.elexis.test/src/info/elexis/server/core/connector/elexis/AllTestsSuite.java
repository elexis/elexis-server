package info.elexis.server.core.connector.elexis;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import info.elexis.server.core.connector.elexis.billable.BillingTest;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarTest;
import info.elexis.server.core.connector.elexis.jpa.AbstractDBObjectIdDeletedTest;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;
import info.elexis.server.core.connector.elexis.services.ArtikelServiceTest;
import info.elexis.server.core.connector.elexis.services.ArtikelstammItemServiceTest;
import info.elexis.server.core.connector.elexis.services.BehandlungServiceTest;
import info.elexis.server.core.connector.elexis.services.DocHandleServiceTest;
import info.elexis.server.core.connector.elexis.services.EigenleistungServiceTest;
import info.elexis.server.core.connector.elexis.services.InvoiceServiceTest;
import info.elexis.server.core.connector.elexis.services.JPAQueryTest;
import info.elexis.server.core.connector.elexis.services.KontaktServiceTest;
import info.elexis.server.core.connector.elexis.services.LabOrderServiceTest;
import info.elexis.server.core.connector.elexis.services.LabResultServiceTest;
import info.elexis.server.core.connector.elexis.services.LockServiceTest;
import info.elexis.server.core.connector.elexis.services.PrescriptionServiceTest;
import info.elexis.server.core.connector.elexis.services.StockServiceTest;
import info.elexis.server.core.connector.elexis.services.StoreToStringTest;
import info.elexis.server.core.connector.elexis.services.UserconfigServiceTest;

@RunWith(Suite.class)
@SuiteClasses({ AbstractDBObjectIdDeletedTest.class, ArtikelServiceTest.class, ArtikelstammItemServiceTest.class,
		BehandlungServiceTest.class, BillingTest.class, DocHandleServiceTest.class, EigenleistungServiceTest.class,
		InvoiceServiceTest.class, JPAQueryTest.class, KontaktServiceTest.class, LabOrderServiceTest.class, LabResultServiceTest.class,
		LockServiceTest.class, PrescriptionServiceTest.class, VerrechenbarTest.class, StockServiceTest.class,
		UserconfigServiceTest.class, StoreToStringTest.class })
public class AllTestsSuite {

	private static TestDatabaseInitializer initializer = new TestDatabaseInitializer();

	@BeforeClass
	public static void setupClass() {
		initializer.initializeDb();

		AllTestsSuite.getInitializer().initializeLaborTarif2009Tables();
		AllTestsSuite.getInitializer().initializeArzttarifePhysioLeistungTables();
		AllTestsSuite.getInitializer().initializeTarmedTables();
		AllTestsSuite.getInitializer().initializeLaborItemsOrdersResults();
	}

	public static TestDatabaseInitializer getInitializer() {
		return initializer;
	}
}
