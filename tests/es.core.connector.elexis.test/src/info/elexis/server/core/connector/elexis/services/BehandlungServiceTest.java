package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.status.ObjectStatus;
import ch.rgw.tools.Money;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarTarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Invoice;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;

public class BehandlungServiceTest extends AbstractServiceTest {

	@Before
	public void initialize() {
		createTestMandantPatientFallBehandlung();
		createTestMandantPatientFallBehandlung();
	}

	@After
	public void after() {
		cleanup();
	}

	public static final String TARMED_KONS_ERSTE_5_MIN = "00.0010";
	public static final String TARMED_KONS_ZUSCHLAG = "00.0015";

	@Test
	public void testGetAllConsultationsForPatient() {
		List<Behandlung> consultations = BehandlungService.findAllConsultationsForPatient(testPatients.get(0));
		assertTrue(consultations.size() > 0);
	}

	@Test
	public void testGetBehandlung() {
		Optional<Behandlung> findById = BehandlungService.load(testBehandlungen.get(0).getId());
		assertTrue(findById.isPresent());
	}

	@Test
	public void testAddMultipleInvaildBillablesToBehandlungFails() {
		Optional<TarmedLeistung> tarmedBasic = TarmedLeistungService.findFromCode(TARMED_KONS_ERSTE_5_MIN);
		assertTrue(tarmedBasic.isPresent());
		VerrechenbarTarmedLeistung vtl = new VerrechenbarTarmedLeistung(tarmedBasic.get());

		IStatus chargeBillableOnBehandlung = BehandlungService.chargeBillableOnBehandlung(testBehandlungen.get(0), vtl,
				testContacts.get(0), testContacts.get(0));
		assertTrue(chargeBillableOnBehandlung.isOK());

		chargeBillableOnBehandlung = BehandlungService.chargeBillableOnBehandlung(testBehandlungen.get(0), vtl,
				testContacts.get(0), testContacts.get(0));
		assertFalse(chargeBillableOnBehandlung.isOK());
	}

	@Test
	public void testAddBillableToInvoicedBehandlung() {
		Optional<TarmedLeistung> tarmedBasic = TarmedLeistungService.findFromCode(TARMED_KONS_ERSTE_5_MIN);
		assertTrue(tarmedBasic.isPresent());
		VerrechenbarTarmedLeistung vtl = new VerrechenbarTarmedLeistung(tarmedBasic.get());

		IStatus chargeBillableOnBehandlung = BehandlungService.chargeBillableOnBehandlung(testBehandlungen.get(0), vtl,
				testContacts.get(0), testContacts.get(0));
		assertTrue(chargeBillableOnBehandlung.isOK());

		Invoice invoice = new InvoiceService.Builder("15", testContacts.get(0), testBehandlungen.get(0).getFall(),
				LocalDate.now().minusWeeks(2), LocalDate.now(), new Money(34.50), InvoiceState.OFFEN).buildAndSave();
		testBehandlungen.get(0).setInvoice(invoice);

		Optional<TarmedLeistung> tarmedZuschlag = TarmedLeistungService.findFromCode(TARMED_KONS_ZUSCHLAG);
		assertTrue(tarmedZuschlag.isPresent());
		VerrechenbarTarmedLeistung vtlZuschlag = new VerrechenbarTarmedLeistung(tarmedZuschlag.get());

		IStatus chargeBillableZuschlagOnBehandlung = BehandlungService.chargeBillableOnBehandlung(
				testBehandlungen.get(0), vtlZuschlag, testContacts.get(0), testContacts.get(0));
		assertTrue(!chargeBillableZuschlagOnBehandlung.isOK());

		InvoiceService.remove(invoice);
	}

	@Test
	public void testConsultationIsEditable() {
		Behandlung kons = testBehandlungen.get(1);
		Kontakt mandator = testContacts.get(1);
		assertEquals(true, BehandlungService.isEditable(kons, mandator).isOK());

		Fall fall = kons.getFall();
		fall.setDatumBis(LocalDate.now());
		FallService.save(fall);

		MultiStatus ms = (MultiStatus) BehandlungService.isEditable(kons, mandator);
		assertEquals(false, ms.isOK());
		assertEquals(1, ms.getChildren().length);

		ms = (MultiStatus) BehandlungService.isEditable(kons, testContacts.get(0));
		assertEquals(false, ms.isOK());
		assertEquals(2, ms.getChildren().length);

		Invoice invoice = new InvoiceService.Builder("26", mandator, kons.getFall(), LocalDate.now().minusWeeks(2),
				LocalDate.now(), new Money(34.50), InvoiceState.OFFEN).buildAndSave();
		kons.setInvoice(invoice);

		ms = (MultiStatus) BehandlungService.isEditable(kons, mandator);
		assertEquals(false, ms.isOK());
		assertEquals(2, ms.getChildren().length);

		ms = (MultiStatus) BehandlungService.isEditable(kons, testContacts.get(0));
		assertEquals(false, ms.isOK());
		assertEquals(3, ms.getChildren().length);

		invoice.setState(InvoiceState.STORNIERT);

		ms = (MultiStatus) BehandlungService.isEditable(kons, mandator);
		assertEquals(false, ms.isOK());
		assertEquals(1, ms.getChildren().length);
	}

}
