package info.elexis.server.core.connector.elexis.billable;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.model.FallConstants;
import ch.elexis.core.status.ObjectStatus;
import ch.elexis.core.types.Gender;
import info.elexis.server.core.connector.elexis.AllTestsSuite;
import info.elexis.server.core.connector.elexis.billable.optifier.TarmedOptifier;
import info.elexis.server.core.connector.elexis.jpa.ElexisTypeMap;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Labor2009Tarif;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.PhysioLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
import info.elexis.server.core.connector.elexis.services.ArtikelstammItemService;
import info.elexis.server.core.connector.elexis.services.BehandlungService;
import info.elexis.server.core.connector.elexis.services.FallService;
import info.elexis.server.core.connector.elexis.services.JPAQuery;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;
import info.elexis.server.core.connector.elexis.services.KontaktService;
import info.elexis.server.core.connector.elexis.services.Labor2009TarifService;
import info.elexis.server.core.connector.elexis.services.PhysioLeistungService;
import info.elexis.server.core.connector.elexis.services.TarmedLeistungService;
import info.elexis.server.core.connector.elexis.services.VerrechnetService;

public class BillingTest {

	private Kontakt patient;
	private Kontakt userContact;
	private Fall testFall;
	private Kontakt mandator;
	private Behandlung consultation;
	private Verrechnet vr;

	@BeforeClass
	public static void init() {
		AllTestsSuite.getInitializer().initializeLaborTarif2009Tables();
		AllTestsSuite.getInitializer().initializeTarmedTables();
		AllTestsSuite.getInitializer().initializeArzttarifePhysioLeistungTables();
	}

	@Before
	public void setupPatientAndBehandlung() {
		patient = KontaktService.INSTANCE.createPatient("Vorname", "Nachname", LocalDate.now(), Gender.FEMALE);
		testFall = FallService.INSTANCE.create(patient, "test", FallConstants.TYPE_DISEASE, "UVG");
		JPAQuery<Kontakt> mandantQuery = new JPAQuery<Kontakt>(Kontakt.class);
		mandantQuery.add(Kontakt_.person, QUERY.EQUALS, true);
		mandantQuery.add(Kontakt_.mandator, QUERY.EQUALS, true);
		List<Kontakt> mandants = mandantQuery.execute();
		assertTrue(!mandants.isEmpty());
		mandator = mandants.get(0);
		userContact = mandator;

		consultation = BehandlungService.INSTANCE.create(testFall, mandator);
	}

	@After
	public void teardownPatientAndBehandlung() {
		if (vr != null) {
			VerrechnetService.INSTANCE.remove(vr);
		}
		BehandlungService.INSTANCE.remove(consultation);
		FallService.INSTANCE.remove(testFall);
		KontaktService.INSTANCE.remove(patient);
	}

	@Test
	public void testAddLaborTarif2009Billing() {
		Labor2009Tarif immunglobulinValid = Labor2009TarifService.INSTANCE.findById("a6e58fc71c723bd54016760").get();
		assertNotNull(immunglobulinValid);
		VerrechenbarLabor2009Tarif validLabTarif = new VerrechenbarLabor2009Tarif(immunglobulinValid);

		IStatus status = validLabTarif.add(consultation, userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);

		assertEquals(12000, vr.getVk_tp());
		assertEquals(12000, vr.getVk_preis());
		assertEquals(100, vr.getScale());
		assertEquals(100, vr.getScale2());
		assertEquals(immunglobulinValid.getId(), vr.getLeistungenCode());
		assertEquals(consultation.getId(), vr.getBehandlung().getId());
		assertEquals(ElexisTypeMap.TYPE_LABOR2009TARIF, vr.getKlasse());
		assertEquals(1, vr.getZahl());

		Labor2009Tarif immunglobulinInvalid = Labor2009TarifService.INSTANCE.findById("ub49a50af4d3e51e40906").get();
		VerrechenbarLabor2009Tarif invalidLabTarif = new VerrechenbarLabor2009Tarif(immunglobulinInvalid);

		status = invalidLabTarif.add(consultation, userContact, mandator);
		assertTrue(status.getMessage(), !status.isOK());
	}

	@Test
	public void testAddLaborTarif2009BillingFindByCode() {
		Labor2009Tarif immunglobulinValid = Labor2009TarifService.findFromCode("1442.00").get();
		assertNotNull(immunglobulinValid);
		VerrechenbarLabor2009Tarif validLabTarif = new VerrechenbarLabor2009Tarif(immunglobulinValid);
		IStatus status = validLabTarif.add(consultation, userContact, mandator);
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
		IStatus status = validPhysTarif.add(consultation, userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);

		assertEquals("0.92", vr.getVk_scale());
		assertEquals(4800, vr.getVk_tp());
		assertEquals(4416, vr.getVk_preis());
		assertEquals(100, vr.getScale());
		assertEquals(100, vr.getScale2());
		assertEquals(validDefault.getId(), vr.getLeistungenCode());
		assertEquals(consultation.getId(), vr.getBehandlung().getId());
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

		IStatus status = vlt_000010.add(consultation, userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);

		assertEquals("0.92", vr.getVk_scale());
		assertEquals(1776, vr.getVk_tp());
		assertEquals(1634, vr.getVk_preis());
		assertEquals(100, vr.getScale());
		assertEquals(100, vr.getScale2());
		assertEquals(ElexisTypeMap.TYPE_TARMEDLEISTUNG, vr.getKlasse());
		assertEquals(consultation.getId(), vr.getBehandlung().getId());
		assertEquals(1, vr.getZahl());

		VerrechenbarTarmedLeistung ivlt_000750 = new VerrechenbarTarmedLeistung(code_000750);
		status = ivlt_000750.add(consultation, userContact, mandator);
		assertTrue(status.getMessage(), !status.isOK());
	}

	@Test
	public void testDoNotBillTympanometrieTwicePerSideTicket5004() {
		TarmedLeistung code_090510 = TarmedLeistungService.findFromCode("09.0510", null).get();
		assertNotNull(code_090510);
		VerrechenbarTarmedLeistung vtl_090510 = new VerrechenbarTarmedLeistung(code_090510);

		IStatus status = vtl_090510.add(consultation, userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);

		status = vtl_090510.add(consultation, userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);

		status = vtl_090510.add(consultation, userContact, mandator);
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

		IStatus status = tlUltrasoundV.add(consultation, userContact, mandator);
		assertTrue(status.isOK());
		status = tlBaseXRayV.add(consultation, userContact, mandator);
		assertFalse(status.isOK());
		status = tlTapingCat1V.add(consultation, userContact, mandator);
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

		IStatus status = tlBaseXRayV.add(consultation, userContact, mandator);
		assertTrue(status.isOK());
		status = tlUltrasoundV.add(consultation, userContact, mandator);
		assertFalse(status.isOK());
		status = tlBaseRadiologyHospitalV.add(consultation, userContact, mandator);
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

		artikelstammItem.get().setIstbestand(2);
		artikelstammItem.get().setMinbestand(2);
		artikelstammItem.get().setMaxbestand(3);

		ArtikelstammItemService.INSTANCE.write(artikelstammItem.get());

		VerrechenbarArtikelstammItem verrechenbar = new VerrechenbarArtikelstammItem(artikelstammItem.get());

		IStatus status = verrechenbar.add(consultation, userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);

		assertEquals(ElexisTypeMap.TYPE_ARTIKELSTAMM, vr.getKlasse());
		assertEquals(consultation.getId(), vr.getBehandlung().getId());
		assertEquals(1, vr.getZahl());

		artikelstammItem = ArtikelstammItemService.findByGTIN("7680531600264");
		assertTrue(artikelstammItem.isPresent());
		assertEquals(3, artikelstammItem.get().getMaxbestand());
		assertEquals(2, artikelstammItem.get().getMinbestand());
		assertEquals(1, artikelstammItem.get().getIstbestand());
	}
}
