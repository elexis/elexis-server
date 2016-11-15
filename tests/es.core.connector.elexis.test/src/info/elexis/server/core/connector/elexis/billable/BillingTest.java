package info.elexis.server.core.connector.elexis.billable;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.status.ObjectStatus;
import ch.elexis.core.stock.IStockEntry;
import info.elexis.server.core.connector.elexis.billable.optifier.TarmedOptifier;
import info.elexis.server.core.connector.elexis.jpa.ElexisTypeMap;
import info.elexis.server.core.connector.elexis.jpa.StoreToStringService;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Artikel;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Labor2009Tarif;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.PhysioLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Stock;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.StockEntry;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
import info.elexis.server.core.connector.elexis.services.AbstractServiceTest;
import info.elexis.server.core.connector.elexis.services.ArtikelService;
import info.elexis.server.core.connector.elexis.services.ArtikelstammItemService;
import info.elexis.server.core.connector.elexis.services.BehandlungService;
import info.elexis.server.core.connector.elexis.services.JPAQuery;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;
import info.elexis.server.core.connector.elexis.services.Labor2009TarifService;
import info.elexis.server.core.connector.elexis.services.PhysioLeistungService;
import info.elexis.server.core.connector.elexis.services.StockEntryService;
import info.elexis.server.core.connector.elexis.services.StockService;
import info.elexis.server.core.connector.elexis.services.TarmedLeistungService;
import info.elexis.server.core.connector.elexis.services.VerrechnetService;

public class BillingTest extends AbstractServiceTest {

	private static Kontakt userContact;
	private static Kontakt mandator;

	private Verrechnet vr;

	@BeforeClass
	public static void init() {
		JPAQuery<Kontakt> mandantQuery = new JPAQuery<Kontakt>(Kontakt.class);
		mandantQuery.add(Kontakt_.person, QUERY.EQUALS, true);
		mandantQuery.add(Kontakt_.mandator, QUERY.EQUALS, true);
		List<Kontakt> mandants = mandantQuery.execute();
		assertTrue(!mandants.isEmpty());
		mandator = mandants.get(0);
		userContact = mandator;
	}

	@Before
	public void before() {
		createTestMandantPatientFallBehandlung();
		createTestMandantPatientFallBehandlung();
	}

	@After
	public void teardownPatientAndBehandlung() {
		cleanup();
	}

	@Test
	public void testBehandlungResolvesVerrechnet() {
		TarmedLeistung code_000010 = TarmedLeistungService.findFromCode("00.0010", null).get();
		VerrechenbarTarmedLeistung vlt_000010 = new VerrechenbarTarmedLeistung(code_000010);
		IStatus status = vlt_000010.add(testBehandlungen.get(0), userContact, null);

		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);
		assertTrue(os.isOK());

		assertTrue(vr.getBehandlung().getVerrechnet().size() > 0);
		assertTrue(vr.getBehandlung().getVerrechnet().contains(vr));
	}

	@Test
	public void testAddLaborTarif2009Billing() {
		Labor2009Tarif immunglobulinValid = Labor2009TarifService.INSTANCE.findById("a6e58fc71c723bd54016760").get();
		assertNotNull(immunglobulinValid);
		VerrechenbarLabor2009Tarif validLabTarif = new VerrechenbarLabor2009Tarif(immunglobulinValid);

		IStatus status = validLabTarif.add(testBehandlungen.get(0), userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);

		assertEquals(immunglobulinValid.getName(), vr.getLeistungenText());
		assertEquals(12000, vr.getVk_tp());
		assertEquals(12000, vr.getVk_preis());
		assertEquals(100, vr.getScale());
		assertEquals(100, vr.getScale2());
		assertEquals(immunglobulinValid.getId(), vr.getLeistungenCode());
		assertEquals(testBehandlungen.get(0).getId(), vr.getBehandlung().getId());
		assertEquals(ElexisTypeMap.TYPE_LABOR2009TARIF, vr.getKlasse());
		assertEquals(1, vr.getZahl());

		Labor2009Tarif immunglobulinInvalid = Labor2009TarifService.INSTANCE.findById("ub49a50af4d3e51e40906").get();
		VerrechenbarLabor2009Tarif invalidLabTarif = new VerrechenbarLabor2009Tarif(immunglobulinInvalid);

		status = invalidLabTarif.add(testBehandlungen.get(0), userContact, mandator);
		assertTrue(status.getMessage(), !status.isOK());
	}

	@Test
	public void testAddLaborTarif2009BillingFindByCode() {
		Labor2009Tarif immunglobulinValid = Labor2009TarifService.findFromCode("1442.00").get();
		assertNotNull(immunglobulinValid);
		VerrechenbarLabor2009Tarif validLabTarif = new VerrechenbarLabor2009Tarif(immunglobulinValid);
		IStatus status = validLabTarif.add(testBehandlungen.get(0), userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);
	}

	@Test
	public void testAddPhysioLeistungBilling() {
		PhysioLeistung validDefault = PhysioLeistungService.findFromCode("7301").get();
		assertNotNull(validDefault);
		VerrechenbarPhysioLeistung validPhysTarif = new VerrechenbarPhysioLeistung(validDefault);
		IStatus status = validPhysTarif.add(testBehandlungen.get(0), userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);

		assertEquals(validDefault.getTitel(), vr.getLeistungenText());
		assertEquals("0.89", vr.getVk_scale());
		assertEquals(4800, vr.getVk_tp());
		assertEquals(4272, vr.getVk_preis());
		assertEquals(100, vr.getScale());
		assertEquals(100, vr.getScale2());
		assertEquals(validDefault.getId(), vr.getLeistungenCode());
		assertEquals(testBehandlungen.get(0).getId(), vr.getBehandlung().getId());
		assertEquals(ElexisTypeMap.TYPE_PHYSIOLEISTUNG, vr.getKlasse());
		assertEquals(1, vr.getZahl());
	}

	@Test
	public void testAddTarmedBilling() {
		TarmedLeistung code_000010 = TarmedLeistungService.findFromCode("00.0010", null).get();
		TarmedLeistung code_000015 = TarmedLeistungService.findFromCode("00.0015", null).get();
		TarmedLeistung code_000750 = TarmedLeistungService.findFromCode("00.0750", null).get();
		assertNotNull(code_000015);
		assertNotNull(code_000010);
		assertNotNull(code_000750);

		VerrechenbarTarmedLeistung vlt_000010 = new VerrechenbarTarmedLeistung(code_000010);

		IStatus status = vlt_000010.add(testBehandlungen.get(0), userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);

		assertEquals(code_000010.getTx255(), vr.getLeistungenText());
		assertEquals("0.89", vr.getVk_scale());
		assertEquals(1776, vr.getVk_tp());
		assertEquals(1581, vr.getVk_preis());
		assertEquals(100, vr.getScale());
		assertEquals(100, vr.getScale2());
		assertEquals(ElexisTypeMap.TYPE_TARMEDLEISTUNG, vr.getKlasse());
		assertEquals(testBehandlungen.get(0).getId(), vr.getBehandlung().getId());
		assertEquals(1, vr.getZahl());

		VerrechenbarTarmedLeistung ivlt_000750 = new VerrechenbarTarmedLeistung(code_000750);
		status = ivlt_000750.add(testBehandlungen.get(0), userContact, mandator);
		assertTrue(status.getMessage(), !status.isOK());
	}

	@Test
	public void testDoNotBillTympanometrieTwicePerSideTicket5004() {
		TarmedLeistung code_090510 = TarmedLeistungService.findFromCode("09.0510", null).get();
		assertNotNull(code_090510);
		VerrechenbarTarmedLeistung vtl_090510 = new VerrechenbarTarmedLeistung(code_090510);

		IStatus status = vtl_090510.add(testBehandlungen.get(0), userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);

		status = vtl_090510.add(testBehandlungen.get(0), userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);

		status = vtl_090510.add(testBehandlungen.get(0), userContact, mandator);
		assertFalse(status.getMessage(), status.isOK());
	}

	@Test
	public void testAddCompatibleAndIncompatibleTarmedBilling() {
		TarmedLeistung tlBaseXRay = (TarmedLeistung) TarmedLeistungService.findFromCode("39.0020", null).get();
		TarmedLeistung tlUltrasound = (TarmedLeistung) TarmedLeistungService.findFromCode("39.3005", null).get();
		TarmedLeistung tlTapingCat1 = (TarmedLeistung) TarmedLeistungService.findFromCode("01.0110", null).get();

		VerrechenbarTarmedLeistung tlBaseXRayV = new VerrechenbarTarmedLeistung(tlBaseXRay);
		VerrechenbarTarmedLeistung tlUltrasoundV = new VerrechenbarTarmedLeistung(tlUltrasound);
		VerrechenbarTarmedLeistung tlTapingCat1V = new VerrechenbarTarmedLeistung(tlTapingCat1);

		IStatus status = tlUltrasoundV.add(testBehandlungen.get(0), userContact, mandator);
		assertTrue(status.isOK());
		status = tlBaseXRayV.add(testBehandlungen.get(0), userContact, mandator);
		assertFalse(status.isOK());
		status = tlTapingCat1V.add(testBehandlungen.get(0), userContact, mandator);
		assertTrue(status.isOK());
	}

	@Test
	public void testAddMultipleIncompatibleTarmedBilling() {
		TarmedLeistung tlBaseXRay = (TarmedLeistung) TarmedLeistungService.findFromCode("39.0020", null).get();
		VerrechenbarTarmedLeistung tlBaseXRayV = new VerrechenbarTarmedLeistung(tlBaseXRay);

		TarmedLeistung tlUltrasound = (TarmedLeistung) TarmedLeistungService.findFromCode("39.3005", null).get();
		VerrechenbarTarmedLeistung tlUltrasoundV = new VerrechenbarTarmedLeistung(tlUltrasound);

		TarmedLeistung tlBaseRadiologyHospital = (TarmedLeistung) TarmedLeistungService.findFromCode("39.0015", null)
				.get();
		VerrechenbarTarmedLeistung tlBaseRadiologyHospitalV = new VerrechenbarTarmedLeistung(tlBaseRadiologyHospital);

		IStatus status = tlBaseXRayV.add(testBehandlungen.get(0), userContact, mandator);
		assertTrue(status.isOK());
		status = tlUltrasoundV.add(testBehandlungen.get(0), userContact, mandator);
		assertFalse(status.isOK());
		status = tlBaseRadiologyHospitalV.add(testBehandlungen.get(0), userContact, mandator);
		assertFalse(status.isOK());
	}

	@Test
	public void testIsCompatibleTarmedBilling() {
		TarmedOptifier optifier = new TarmedOptifier();

		TarmedLeistung tlBaseXRay = (TarmedLeistung) TarmedLeistungService.findFromCode("39.0020", null).get();
		TarmedLeistung tlUltrasound = (TarmedLeistung) TarmedLeistungService.findFromCode("39.3005", null).get();
		TarmedLeistung tlBaseRadiologyHospital = (TarmedLeistung) TarmedLeistungService.findFromCode("39.0015", null)
				.get();
		TarmedLeistung tlBaseFirst5Min = (TarmedLeistung) TarmedLeistungService.findFromCode("00.0010", null).get();

		IStatus resCompatible = optifier.isCompatible(tlBaseXRay, tlUltrasound);
		assertFalse(resCompatible.isOK());
		String resText = "";
		if (!resCompatible.getMessage().isEmpty()) {
			resText = resCompatible.getMessage();
		}
		assertEquals("39.3005 nicht kombinierbar mit 39.0020", resText);
		resCompatible = optifier.isCompatible(tlUltrasound, tlBaseXRay);
		assertTrue(resCompatible.isOK());

		resCompatible = optifier.isCompatible(tlBaseXRay, tlBaseRadiologyHospital);
		assertFalse(resCompatible.isOK());
		if (!resCompatible.getMessage().isEmpty()) {
			resText = resCompatible.getMessage();
		}
		assertEquals("39.0015 nicht kombinierbar mit 39.0020", resText);

		resCompatible = optifier.isCompatible(tlBaseRadiologyHospital, tlUltrasound);
		assertFalse(resCompatible.isOK());

		resCompatible = optifier.isCompatible(tlBaseXRay, tlBaseFirst5Min);
		assertTrue(resCompatible.isOK());

		resCompatible = optifier.isCompatible(tlBaseFirst5Min, tlBaseRadiologyHospital);
		assertTrue(resCompatible.isOK());
	}

	@Test
	public void testAddArtikelstammBilling() {
		Optional<ArtikelstammItem> artikelstammItem = ArtikelstammItemService.findByGTIN("7680531600264");
		assertTrue(artikelstammItem.isPresent());

		ArtikelstammItemService.INSTANCE.write(artikelstammItem.get());

		VerrechenbarArtikelstammItem verrechenbar = new VerrechenbarArtikelstammItem(artikelstammItem.get());

		IStatus status = verrechenbar.add(testBehandlungen.get(0), userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);

		assertEquals(ElexisTypeMap.TYPE_ARTIKELSTAMM, vr.getKlasse());
		assertEquals(artikelstammItem.get().getDscr(), vr.getLeistungenText());
		assertEquals(testBehandlungen.get(0).getId(), vr.getBehandlung().getId());
		assertEquals(1, vr.getZahl());
		assertEquals("1.0", vr.getVk_scale());
		Double ppub = Double.valueOf(artikelstammItem.get().getPpub()) * 100;
		assertEquals(ppub.intValue(), vr.getVk_tp());
		assertEquals(ppub.intValue(), vr.getVk_preis());
		assertEquals(100, vr.getScale());
		assertEquals(100, vr.getScale2());

		artikelstammItem = ArtikelstammItemService.findByGTIN("7680531600264");
		assertTrue(artikelstammItem.isPresent());
	}

	@Test
	public void testAddEigenartikelBilling() {
		Artikel ea1 = ArtikelService.INSTANCE.create("NameVerrechnen", "InternalName", Artikel.TYP_EIGENARTIKEL);
		ea1.setEkPreis("150");
		ea1.setVkPreis("300");
		ArtikelService.INSTANCE.write(ea1);

		VerrechenbarArtikel verrechenbar = new VerrechenbarArtikel(ea1);

		IStatus status = verrechenbar.add(testBehandlungen.get(0), userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);
		assertTrue(os.isOK());

		assertEquals(ElexisTypeMap.TYPE_EIGENARTIKEL, vr.getKlasse());
		assertEquals(ea1.getLabel(), vr.getLeistungenText());
		assertEquals(testBehandlungen.get(0).getId(), vr.getBehandlung().getId());
		assertEquals(1, vr.getZahl());
		assertEquals(300, vr.getVk_preis());
		assertEquals(100, vr.getScale());

		ArtikelService.INSTANCE.remove(ea1);
	}

	@Test
	public void testChangeCountAddOnVerrechnetInvalid() {
		TarmedLeistung code_000010 = TarmedLeistungService.findFromCode("00.0010", null).get();
		VerrechenbarTarmedLeistung vlt_000010 = new VerrechenbarTarmedLeistung(code_000010);

		IStatus status = vlt_000010.add(testBehandlungen.get(0), userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);

		IStatus invalid = VerrechnetService.changeCountValidated(vr, 2, mandator);
		assertFalse(invalid.isOK());
		assertEquals(Status.WARNING, invalid.getSeverity());

		VerrechnetService.INSTANCE.refresh(vr);
		assertEquals(1, vr.getZahl());
	}

	@Test
	public void testChangeCountAddOnVerrechnetValid() {
		Optional<ArtikelstammItem> artikelstammItem = ArtikelstammItemService.findByGTIN("7680531600264");
		VerrechenbarArtikelstammItem verrechenbar = new VerrechenbarArtikelstammItem(artikelstammItem.get());

		IStatus status = verrechenbar.add(testBehandlungen.get(1), userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);

		IStatus valid = VerrechnetService.changeCountValidated(vr, 3, mandator);
		assertTrue(valid.isOK());

		assertEquals(1, vr.getBehandlung().getVerrechnet().size());
		assertEquals(3, vr.getBehandlung().getVerrechnet().get(0).getZahl());
		Double ppub = Double.valueOf(artikelstammItem.get().getPpub()) * 100;
		assertEquals(ppub.intValue(), vr.getVk_tp());
		assertEquals(ppub.intValue(), vr.getVk_preis());
	}

	@Test
	public void testChangeCountRemoveOnVerrechnetValid() {
		Optional<ArtikelstammItem> artikelstammItem = ArtikelstammItemService.findByGTIN("7680531600264");
		VerrechenbarArtikelstammItem verrechenbar = new VerrechenbarArtikelstammItem(artikelstammItem.get());

		IStatus status = verrechenbar.add(testBehandlungen.get(1), userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);

		IStatus valid = VerrechnetService.changeCountValidated(vr, 3, mandator);
		assertTrue(valid.isOK());

		BehandlungService.INSTANCE.refresh(testBehandlungen.get(1));
		VerrechnetService.INSTANCE.refresh(vr);

		assertEquals(vr.getId(), testBehandlungen.get(1).getVerrechnet().get(0).getId());
		assertEquals(1, testBehandlungen.get(1).getVerrechnet().size());
		assertEquals(3, testBehandlungen.get(1).getVerrechnet().get(0).getZahl());
		Double ppub = Double.valueOf(artikelstammItem.get().getPpub()) * 100;
		assertEquals(ppub.intValue(), vr.getVk_tp());
		assertEquals(ppub.intValue(), vr.getVk_preis());

		valid = VerrechnetService.changeCountValidated(testBehandlungen.get(1).getVerrechnet().get(0), 1, mandator);
		assertTrue(valid.isOK());

		BehandlungService.INSTANCE.refresh(testBehandlungen.get(1));

		assertEquals(1, testBehandlungen.get(1).getVerrechnet().size());
		assertEquals(1, testBehandlungen.get(1).getVerrechnet().get(0).getZahl());
	}

	@Test
	public void testStockRemovalOnArticleDisposal() {
		Artikel ea1 = ArtikelService.INSTANCE.create("NameVerrechnen", "InternalName", Artikel.TYP_EIGENARTIKEL);
		ea1.setEkPreis("150");
		ea1.setVkPreis("300");
		ArtikelService.INSTANCE.write(ea1);

		StockService stockService = StockService.INSTANCE;
		Stock defaultStock = StockService.INSTANCE.findById("STD").get();
		IStockEntry se = stockService.storeArticleInStock(defaultStock, StoreToStringService.storeToString(ea1));
		se.setMinimumStock(5);
		se.setCurrentStock(10);
		se.setMaximumStock(15);
		StockEntryService.INSTANCE.write((StockEntry) se);

		VerrechenbarArtikel verrechenbar = new VerrechenbarArtikel(ea1);

		IStatus status = verrechenbar.add(testBehandlungen.get(0), userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);
		assertTrue(os.isOK());

		assertEquals(ElexisTypeMap.TYPE_EIGENARTIKEL, vr.getKlasse());
		assertEquals(ea1.getLabel(), vr.getLeistungenText());
		assertEquals(testBehandlungen.get(0).getId(), vr.getBehandlung().getId());
		assertEquals(1, vr.getZahl());
		assertEquals(300, vr.getVk_preis());
		assertEquals(100, vr.getScale());

		Integer stockValue = stockService.getCumulatedStockForArticle(ea1);
		assertEquals(9, stockValue.intValue());

		ArtikelService.INSTANCE.remove(ea1);
	}
}
