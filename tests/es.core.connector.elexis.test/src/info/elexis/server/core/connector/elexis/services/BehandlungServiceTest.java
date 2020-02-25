//package info.elexis.server.core.connector.elexis.services;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//import java.util.Set;
//
//import org.eclipse.core.runtime.IStatus;
//import org.eclipse.core.runtime.MultiStatus;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import ch.elexis.core.model.InvoiceState;
//import ch.rgw.tools.Money;
//import info.elexis.server.core.connector.elexis.AllTestsSuite;
//import info.elexis.server.core.connector.elexis.billable.VerrechenbarTarmedLeistung;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Diagnosis;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Diagnosis_;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Invoice;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;
//import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;
//
//public class BehandlungServiceTest extends AbstractServiceTest {
//
//	@Before
//	public void initialize() {
//		createTestMandantPatientFallBehandlung();
//		createTestMandantPatientFallBehandlung();
//	}
//
//	@After
//	public void after() {
//		cleanup();
//	}
//
//	public static final String TARMED_KONS_ERSTE_5_MIN = "00.0010";
//	public static final String TARMED_KONS_ZUSCHLAG = "00.0015";
//
//	@Test
//	public void testGetAllConsultationsForPatient() {
//		List<Behandlung> consultations = BehandlungService.findAllConsultationsForPatient(testPatients.get(0));
//		assertTrue(consultations.size() > 0);
//	}
//
//	@Test
//	public void testGetBehandlung() {
//		Behandlung findById = BehandlungService.load(testBehandlungen.get(0).getId()).get();
//		assertEquals(LocalDate.now(), findById.getDatum());
//	}
//
//	@Test
//	public void testAddMultipleInvaildBillablesToBehandlungFails() {
//		Optional<TarmedLeistung> tarmedBasic = TarmedLeistungService.findFromCode(TARMED_KONS_ERSTE_5_MIN);
//		assertTrue(tarmedBasic.isPresent());
//		VerrechenbarTarmedLeistung vtl = new VerrechenbarTarmedLeistung(tarmedBasic.get());
//
//		IStatus chargeBillableOnBehandlung = BehandlungService.chargeBillableOnBehandlung(testBehandlungen.get(0), vtl,
//				testContacts.get(0), testContacts.get(0));
//		assertTrue(chargeBillableOnBehandlung.isOK());
//
//		chargeBillableOnBehandlung = BehandlungService.chargeBillableOnBehandlung(testBehandlungen.get(0), vtl,
//				testContacts.get(0), testContacts.get(0));
//		assertFalse(chargeBillableOnBehandlung.isOK());
//	}
//
//	@Test
//	public void testAddBillableToInvoicedBehandlung() {
//		Optional<TarmedLeistung> tarmedBasic = TarmedLeistungService.findFromCode(TARMED_KONS_ERSTE_5_MIN);
//		assertTrue(tarmedBasic.isPresent());
//		VerrechenbarTarmedLeistung vtl = new VerrechenbarTarmedLeistung(tarmedBasic.get());
//
//		IStatus chargeBillableOnBehandlung = BehandlungService.chargeBillableOnBehandlung(testBehandlungen.get(0), vtl,
//				testContacts.get(0), testContacts.get(0));
//		assertTrue(chargeBillableOnBehandlung.isOK());
//
//		Invoice invoice = new InvoiceService.Builder("15", testContacts.get(0), testBehandlungen.get(0).getFall(),
//				LocalDate.now().minusWeeks(2), LocalDate.now(), new Money(34.50), InvoiceState.OPEN).buildAndSave();
//		testBehandlungen.get(0).setInvoice(invoice);
//
//		Optional<TarmedLeistung> tarmedZuschlag = TarmedLeistungService.findFromCode(TARMED_KONS_ZUSCHLAG);
//		assertTrue(tarmedZuschlag.isPresent());
//		VerrechenbarTarmedLeistung vtlZuschlag = new VerrechenbarTarmedLeistung(tarmedZuschlag.get());
//
//		IStatus chargeBillableZuschlagOnBehandlung = BehandlungService.chargeBillableOnBehandlung(
//				testBehandlungen.get(0), vtlZuschlag, testContacts.get(0), testContacts.get(0));
//		assertTrue(!chargeBillableZuschlagOnBehandlung.isOK());
//
//		InvoiceService.remove(invoice);
//	}
//
//	@Test
//	public void testResolveAllCareProvidersForConsultation() {
//		Set<String> careProviderIds = BehandlungService
//				.getAllCareProviderIdsForConsultation(AllTestsSuite.getInitializer().getBehandlung());
//		assertEquals(2, careProviderIds.size());
//		assertTrue(careProviderIds.contains("Administrator"));
//		assertTrue(careProviderIds.contains("user"));
//	}
//
//}
