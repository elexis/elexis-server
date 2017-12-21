package info.elexis.server.core.connector.elexis.billable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.service.event.Event;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.status.ObjectStatus;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.billable.optifier.TarmedOptifier;
import info.elexis.server.core.connector.elexis.jpa.ElexisTypeMap;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
import info.elexis.server.core.connector.elexis.jpa.test.common.VerrechnetMatch;
import info.elexis.server.core.connector.elexis.jpa.test.eventHandler.TestEventHandler;
import info.elexis.server.core.connector.elexis.services.AbstractServiceTest;
import info.elexis.server.core.connector.elexis.services.BehandlungService;
import info.elexis.server.core.connector.elexis.services.TarmedLeistungService;
import info.elexis.server.core.connector.elexis.services.VerrechnetService;

public class TarmedBillingTest extends AbstractServiceTest {

	private static Kontakt userContact;
	private static Kontakt mandator;

	private TarmedLeistung code_000010 = TarmedLeistungService.findFromCode("00.0010", null).get();
	private TarmedLeistung code_000015 = TarmedLeistungService.findFromCode("00.0015", null).get();
	private TarmedLeistung code_000140 = TarmedLeistungService.findFromCode("00.0140", null).get();
	private TarmedLeistung code_000510 = TarmedLeistungService.findFromCode("00.0510", null).get();

	private Verrechnet vr;

	@Before
	public void before() {
		createTestMandantPatientFallBehandlung();
		createTestMandantPatientFallBehandlung();
		createTestMandantPatientFallBehandlung();
		createTestMandantPatientFallBehandlung();
		createTestMandantPatientFallBehandlung();
		createTestMandantPatientFallBehandlung();
	}

	@After
	public void teardownPatientAndBehandlung() {
		cleanup();
	}

	@Test
	public void testAddTarmedBilling() {
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
		assertEquals(TarmedOptifier.SIDE_L, vr.getDetail().get(TarmedOptifier.SIDE));

		status = vtl_090510.add(testBehandlungen.get(0), userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);
		assertEquals(TarmedOptifier.SIDE_R, vr.getDetail().get(TarmedOptifier.SIDE));

		status = vtl_090510.add(testBehandlungen.get(0), userContact, mandator);
		assertFalse(status.getMessage(), status.isOK());
	}

	@Test
	public void testBehandlungResolvesVerrechnet() {
		VerrechenbarTarmedLeistung vlt_000010 = new VerrechenbarTarmedLeistung(code_000010);

		IStatus status = vlt_000010.add(testBehandlungen.get(0), userContact, null);
		Event event = TestEventHandler.waitforEvent();
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);
		assertTrue(os.isOK());
		TestEventHandler.assertCreateEvent(event, ElexisTypeMap.TYPE_VERRECHNET);
		assertEquals(vr.getId(), event.getProperty(ElexisEventTopics.PROPKEY_ID));
		assertEquals("testUserId", event.getProperty(ElexisEventTopics.PROPKEY_USER));

		List<Verrechnet> allVerrechnet = VerrechnetService.getAllVerrechnetForBehandlung(vr.getBehandlung());
		assertTrue(allVerrechnet.size() > 0);
		boolean contains = false;
		// https://redmine.medelexis.ch/projects/incomingtickets/time_entries?issue_id=5455
		// replaced allVerrechnet.contains(vr)
		for (Verrechnet verrechnet : allVerrechnet) {
			if (vr.getId().equals(verrechnet.getId())) {
				contains = true;
				break;
			}
		}
		assertTrue(contains);
	}

	@Test
	public void testChangeCountAddOnVerrechnetInvalid() {
		VerrechenbarTarmedLeistung vlt_000010 = new VerrechenbarTarmedLeistung(code_000010);

		IStatus status = vlt_000010.add(testBehandlungen.get(0), userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);

		IStatus invalid = VerrechnetService.changeCountValidated(vr, 2, mandator);
		assertFalse(invalid.isOK());
		assertEquals(Status.WARNING, invalid.getSeverity());

		assertEquals(1, vr.getZahl());
	}

	@Test
	public void testAddOnVerrechnetInvalid() {
		VerrechenbarTarmedLeistung vlt_000010 = new VerrechenbarTarmedLeistung(code_000010);

		IStatus status = vlt_000010.add(testBehandlungen.get(3), userContact, mandator, 2);
		assertFalse(status.isOK());
		assertEquals(Status.WARNING, status.getSeverity());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();

		assertEquals(1, vr.getZahl());
	}

	@Test
	public void testMultipleAddsTicket5865() {
		Behandlung behandlung = testBehandlungen.get(4);
		Kontakt myMandator = testContacts.get(4);

		IStatus os;
		VerrechenbarTarmedLeistung vlt_000010 = new VerrechenbarTarmedLeistung(code_000010);
		os = BehandlungService.chargeBillableOnBehandlung(behandlung, vlt_000010, userContact, myMandator);
		assertTrue(os.isOK());
		VerrechenbarTarmedLeistung vlt_000015 = new VerrechenbarTarmedLeistung(code_000015);
		os = BehandlungService.chargeBillableOnBehandlung(behandlung, vlt_000015, userContact, myMandator);
		assertTrue(os.isOK());
		VerrechenbarTarmedLeistung vlt_000140 = new VerrechenbarTarmedLeistung(code_000140);
		os = BehandlungService.chargeBillableOnBehandlung(behandlung, vlt_000140, userContact, myMandator);
		assertTrue(os.isOK());
		VerrechenbarTarmedLeistung vlt_000510 = new VerrechenbarTarmedLeistung(code_000510);
		os = BehandlungService.chargeBillableOnBehandlung(behandlung, vlt_000510, userContact, myMandator, 3);
		assertTrue(os.isOK());

		List<VerrechnetMatch> matches = new ArrayList<>();
		matches.add(new VerrechnetMatch("00.0010-20010101", 1));
		matches.add(new VerrechnetMatch("00.0015-20141001", 1));
		matches.add(new VerrechnetMatch("00.0140-20010101", 1));
		matches.add(new VerrechnetMatch("00.0510-20010101", 3));
		VerrechnetMatch.assertVerrechnetMatch(behandlung, matches);

		os = BehandlungService.chargeBillableOnBehandlung(behandlung, vlt_000010, userContact, myMandator);
		assertFalse(os.isOK());
		os = BehandlungService.chargeBillableOnBehandlung(behandlung, vlt_000015, userContact, myMandator);
		assertFalse(os.isOK());
		os = BehandlungService.chargeBillableOnBehandlung(behandlung, vlt_000140, userContact, myMandator);
		assertTrue(os.isOK());
		vr = (Verrechnet) ((ObjectStatus) os).getObject();
		os = VerrechnetService.changeCountValidated(vr, 1, null);
		os = BehandlungService.chargeBillableOnBehandlung(behandlung, vlt_000510, userContact, myMandator);
		assertTrue(os.isOK());
		vr = (Verrechnet) ((ObjectStatus) os).getObject();
		os = VerrechnetService.changeCountValidated(vr, 2, null);
		assertTrue(os.isOK());

		matches = new ArrayList<>();
		matches.add(new VerrechnetMatch("00.0010-20010101", 1));
		matches.add(new VerrechnetMatch("00.0015-20141001", 1));
		matches.add(new VerrechnetMatch("00.0140-20010101", 1));
		matches.add(new VerrechnetMatch("00.0510-20010101", 2));
		VerrechnetMatch.assertVerrechnetMatch(behandlung, matches);
	}

	@Test
	public void testMultipleIncompatibleBillingIfBillingWasDeleted() {
		VerrechenbarTarmedLeistung vlt_000010 = new VerrechenbarTarmedLeistung(code_000010);

		IStatus status = vlt_000010.add(testBehandlungen.get(2), userContact, mandator);
		assertTrue(status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		Verrechnet vr = (Verrechnet) os.getObject();
		assertNotNull(vr);

		status = vlt_000010.add(testBehandlungen.get(2), userContact, mandator);
		assertFalse(status.isOK());

		vr.setDeleted(true);
		VerrechnetService.save(vr);

		status = vlt_000010.add(testBehandlungen.get(2), userContact, mandator);
		assertTrue(status.isOK());
		os = (ObjectStatus) status;
		Verrechnet vr2 = (Verrechnet) os.getObject();
		assertNotNull(vr2);
		assertNotEquals(vr2, vr);
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

		IStatus resCompatible = optifier.isCompatible(tlBaseXRay, tlUltrasound, testBehandlungen.get(0));
		assertFalse(resCompatible.isOK());
		String resText = "";
		if (!resCompatible.getMessage().isEmpty()) {
			resText = resCompatible.getMessage();
		}
		assertEquals("39.3005 nicht kombinierbar mit Kapitel 39.01", resText);
		resCompatible = optifier.isCompatible(tlUltrasound, tlBaseXRay, testBehandlungen.get(0));
		assertTrue(resCompatible.isOK());

		resCompatible = optifier.isCompatible(tlBaseXRay, tlBaseRadiologyHospital, testBehandlungen.get(0));
		assertFalse(resCompatible.isOK());
		if (!resCompatible.getMessage().isEmpty()) {
			resText = resCompatible.getMessage();
		}
		assertEquals("39.0015 nicht kombinierbar mit Leistung 39.0020", resText);

		resCompatible = optifier.isCompatible(tlBaseRadiologyHospital, tlUltrasound, testBehandlungen.get(0));
		assertFalse(resCompatible.isOK());

		resCompatible = optifier.isCompatible(tlBaseXRay, tlBaseFirst5Min, testBehandlungen.get(0));
		assertTrue(resCompatible.isOK());

		resCompatible = optifier.isCompatible(tlBaseFirst5Min, tlBaseRadiologyHospital, testBehandlungen.get(0));
		assertTrue(resCompatible.isOK());
	}

	@Test
	public void testAddCompatibleAndIncompatibleTarmedBilling() {
		TarmedLeistung tlUltrasound = (TarmedLeistung) TarmedLeistungService.findFromCode("39.3005", null).get();
		TarmedLeistung tlBaseXRay = (TarmedLeistung) TarmedLeistungService.findFromCode("39.0020", null).get();
		TarmedLeistung tlTapingCat1 = (TarmedLeistung) TarmedLeistungService.findFromCode("01.0110", null).get();

		VerrechenbarTarmedLeistung tlBaseXRayV = new VerrechenbarTarmedLeistung(tlBaseXRay);
		VerrechenbarTarmedLeistung tlUltrasoundV = new VerrechenbarTarmedLeistung(tlUltrasound);
		VerrechenbarTarmedLeistung tlTapingCat1V = new VerrechenbarTarmedLeistung(tlTapingCat1);

		IStatus status = tlUltrasoundV.add(testBehandlungen.get(0), userContact, mandator);
		assertTrue(status.isOK());
		status = tlBaseXRayV.add(testBehandlungen.get(0), userContact, mandator);
		assertFalse(status.isOK());
		status = tlTapingCat1V.add(testBehandlungen.get(0), userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
	}

	@Test
	public void testChargeBillableAndChangeCountTicket5484() {
		TarmedLeistung code_000020 = TarmedLeistungService.findFromCode("00.0020", null).get();
		VerrechenbarTarmedLeistung vlt_000020 = new VerrechenbarTarmedLeistung(code_000020);
		IStatus status = BehandlungService.chargeBillableOnBehandlung(testBehandlungen.get(3), vlt_000020, userContact,
				null);
		assertTrue(status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		Verrechnet vr = (Verrechnet) os.getObject();
		IStatus ccStatus = VerrechnetService.changeCountValidated(vr, 2, null);
		assertTrue(ccStatus.isOK());
		vr = (Verrechnet) ((ObjectStatus) ccStatus).getObject();
		assertEquals(2, vr.getZahl());
		assertEquals(100, vr.getScale());
		assertEquals(100, vr.getScale2());

		List<Verrechnet> allVerrechnetForBehandlung = VerrechnetService
				.getAllVerrechnetForBehandlung(testBehandlungen.get(3));
		assertEquals(1, allVerrechnetForBehandlung.size());
		assertEquals(2, allVerrechnetForBehandlung.get(0).getZahl());

		TarmedLeistung code_000510 = TarmedLeistungService.findFromCode("00.0510", null).get();
		VerrechenbarTarmedLeistung vlt_000510 = new VerrechenbarTarmedLeistung(code_000510);
		status = BehandlungService.chargeBillableOnBehandlung(testBehandlungen.get(3), vlt_000510, userContact, null);
		assertTrue(status.isOK());
		os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		ccStatus = VerrechnetService.changeCountValidated(vr, 3, null);

		assertTrue(ccStatus.isOK());

		allVerrechnetForBehandlung = VerrechnetService.getAllVerrechnetForBehandlung(testBehandlungen.get(3));
		assertEquals(2, allVerrechnetForBehandlung.size());
		assertEquals(3, allVerrechnetForBehandlung.get(1).getZahl());
	}

	@Test
	public void testAddAutoPositions() {
		TarmedLeistung pos1 = (TarmedLeistung) TarmedLeistungService.findFromCode("39.0590", null).get();
		TarmedLeistung pos2 = (TarmedLeistung) TarmedLeistungService.findFromCode("39.2000", null).get();
		TarmedLeistung pos3 = (TarmedLeistung) TarmedLeistungService.findFromCode("39.0020", null).get();
		VerrechenbarTarmedLeistung v1 = new VerrechenbarTarmedLeistung(pos1);

		IStatus status = v1.add(testBehandlungen.get(0), userContact, mandator);
		assertTrue(status.isOK());
		List<Verrechnet> lst = VerrechnetService.getAllVerrechnetForBehandlung(testBehandlungen.get(0));
		assertEquals(3, lst.size());
		assertEquals(pos1.getId(), lst.get(0).getLeistungenCode());
		assertEquals(pos2.getId(), lst.get(1).getLeistungenCode());
		assertEquals(pos3.getId(), lst.get(2).getLeistungenCode());
	}
	
	@Test
	public void testResolveTarmedViaLaw() {
		// No UVG for 39.0021, find replacement
		TimeTool date2017 = new TimeTool(LocalDate.of(2017, 12, 21));
		Optional<TarmedLeistung> result = TarmedLeistungService.findFromCode("39.0020", date2017, "UVG");
		assertTrue(result.isPresent());
		assertEquals("39.0020-20141001", result.get().getId());
		result = TarmedLeistungService.findFromCode("39.0020", date2017, "KVG");
		assertTrue(result.isPresent());
		assertEquals("39.0020-20141001-KVG", result.get().getId());
		result = TarmedLeistungService.findFromCode("39.0020", date2017, null);
		assertTrue(result.isPresent());
		assertEquals("39.0020-20141001", result.get().getId());
		
		TimeTool date2018 = new TimeTool(LocalDate.of(2018, 12, 21));
		result = TarmedLeistungService.findFromCode("39.0020", date2018, "UVG");
		assertTrue(result.isPresent());
		assertEquals("39.0020-20141001", result.get().getId());
		result = TarmedLeistungService.findFromCode("39.0020", date2018, "KVG");
		assertTrue(result.isPresent());
		assertEquals("39.0020-20180101-KVG", result.get().getId());
		result = TarmedLeistungService.findFromCode("39.0020", date2018, null);
		assertTrue(result.isPresent());
		assertEquals("39.0020-20141001", result.get().getId());
	}
	
}
