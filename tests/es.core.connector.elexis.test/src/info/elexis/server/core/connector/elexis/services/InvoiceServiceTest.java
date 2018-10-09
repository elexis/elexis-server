//package info.elexis.server.core.connector.elexis.services;
//
//import static org.junit.Assert.*;
//
//import java.time.LocalDate;
//import java.util.Optional;
//
//import org.exparity.hamcrest.date.LocalDateMatchers;
//import org.hamcrest.MatcherAssert;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import ch.elexis.core.model.InvoiceState;
//import ch.rgw.tools.Money;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Invoice;
//
//public class InvoiceServiceTest extends AbstractServiceTest {
//
//	@Before
//	public void before() {
//		createTestMandantPatientFallBehandlung();
//	}
//
//	@After
//	public void after() {
//		cleanup();
//	}
//
//	@Test
//	public void testBuilder() {
//		LocalDate fromDate = LocalDate.now().minusWeeks(2);
//		Invoice invoice = new InvoiceService.Builder("26", testContacts.get(0), testFaelle.get(0), fromDate,
//				LocalDate.now(), new Money(34.50), InvoiceState.OPEN).buildAndSave();
//		Optional<Invoice> load = InvoiceService.load(invoice.getId());
//		assertTrue(load.isPresent());
//		Invoice loaded = load.get();
//		assertEquals("26", loaded.getNumber());
//		assertEquals(testContacts.get(0), loaded.getMandator());
//		assertEquals(testFaelle.get(0), loaded.getFall());
//		assertEquals(fromDate, loaded.getInvoiceDateFrom());
//		assertEquals(new Money(34.50).toString(), loaded.getAmount());
//		assertEquals(InvoiceState.OPEN, loaded.getState());
//		MatcherAssert.assertThat(invoice.getInvoiceDate()	, LocalDateMatchers.sameOrAfter(LocalDate.now()));
//	}
//
//}
