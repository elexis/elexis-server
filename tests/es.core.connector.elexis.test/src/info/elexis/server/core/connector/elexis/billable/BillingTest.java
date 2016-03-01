package info.elexis.server.core.connector.elexis.billable;

import static org.junit.Assert.*;

import org.eclipse.core.runtime.IStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.FallConstants;
import info.elexis.server.core.common.ObjectStatus;
import info.elexis.server.core.connector.elexis.billable.optifier.TarmedOptifier;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Labor2009Tarif;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
import info.elexis.server.core.connector.elexis.services.BehandlungService;
import info.elexis.server.core.connector.elexis.services.FallService;
import info.elexis.server.core.connector.elexis.services.KontaktService;
import info.elexis.server.core.connector.elexis.services.Labor2009TarifService;
import info.elexis.server.core.connector.elexis.services.TarmedLeistungService;
import info.elexis.server.core.connector.elexis.services.VerrechnetService;

public class BillingTest {

	private Kontakt patient;
	private Fall testFall;
	private Kontakt mandator;
	private Behandlung consultation;
	private Verrechnet vr;

	@Before
	public void setupPatientAndBehandlung() {
		patient = KontaktService.INSTANCE.createPatient();
		testFall = FallService.INSTANCE.create(patient, "test", FallConstants.TYPE_DISEASE, "UVG");
		mandator = KontaktService.INSTANCE.findById("td741d2ac3354679104"); // dz
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

		Labor2009Tarif immunglobulinValid = Labor2009TarifService.INSTANCE.findById("a6e58fc71c723bd54016760");
		assertNotNull(immunglobulinValid);
		VerrechenbarLabor2009Tarif validLabTarif = new VerrechenbarLabor2009Tarif(immunglobulinValid);

		IStatus status = validLabTarif.add(consultation, patient, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);

		Labor2009Tarif immunglobulinInvalid = Labor2009TarifService.INSTANCE.findById("ub49a50af4d3e51e40906");
		VerrechenbarLabor2009Tarif invalidLabTarif = new VerrechenbarLabor2009Tarif(immunglobulinInvalid);

		status = invalidLabTarif.add(consultation, patient, mandator);
		assertTrue(status.getMessage(), !status.isOK());
	}

	@Test
	public void testAddTarmedBilling() {
		TarmedLeistung code_000010 = TarmedLeistungService.findFromCode("00.0010", null);
		TarmedLeistung code_000015 = TarmedLeistungService.findFromCode("00.0015", null);
		TarmedLeistung code_000750 = TarmedLeistungService.findFromCode("00.0750", null);
		assertNotNull(code_000015);
		assertNotNull(code_000010);
		assertNotNull(code_000750);

		VerrechenbarTarmedLeistung vlt_000010 = new VerrechenbarTarmedLeistung(code_000010);

		IStatus status = vlt_000010.add(consultation, patient, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);

		assertEquals("0.92", vr.getVk_scale());
		assertEquals(1776, vr.getVk_tp());
		assertEquals(1634, vr.getVk_preis());
		assertEquals(100, vr.getScale());
		assertEquals(100, vr.getScale2());

		VerrechenbarTarmedLeistung ivlt_000750 = new VerrechenbarTarmedLeistung(code_000750);
		status = ivlt_000750.add(consultation, patient, mandator);
		assertTrue(status.getMessage(), !status.isOK());
	}

	@Test
	public void testAddCompatibleAndIncompatibleTarmedBilling() {
		TarmedLeistung tlBaseXRay = (TarmedLeistung) TarmedLeistungService.findFromCode("39.0020", null);
		TarmedLeistung tlUltrasound = (TarmedLeistung) TarmedLeistungService.findFromCode("39.3005", null);
		TarmedLeistung tlTapingCat1 = (TarmedLeistung) TarmedLeistungService.findFromCode("01.0110", null);

		VerrechenbarTarmedLeistung tlBaseXRayV = new VerrechenbarTarmedLeistung(tlBaseXRay);
		VerrechenbarTarmedLeistung tlUltrasoundV = new VerrechenbarTarmedLeistung(tlUltrasound);
		VerrechenbarTarmedLeistung tlTapingCat1V = new VerrechenbarTarmedLeistung(tlTapingCat1);

		IStatus status = tlUltrasoundV.add(consultation, patient, mandator);
		assertTrue(status.isOK());
		status = tlBaseXRayV.add(consultation, patient, mandator);
		assertFalse(status.isOK());
		status = tlTapingCat1V.add(consultation, patient, mandator);
		assertTrue(status.isOK());
	}

	@Test
	public void testAddMultipleIncompatibleTarmedBilling() {
		TarmedLeistung tlBaseXRay = (TarmedLeistung) TarmedLeistungService.findFromCode("39.0020", null);
		VerrechenbarTarmedLeistung tlBaseXRayV = new VerrechenbarTarmedLeistung(tlBaseXRay);

		TarmedLeistung tlUltrasound = (TarmedLeistung) TarmedLeistungService.findFromCode("39.3005", null);
		VerrechenbarTarmedLeistung tlUltrasoundV = new VerrechenbarTarmedLeistung(tlUltrasound);

		TarmedLeistung tlBaseRadiologyHospital = (TarmedLeistung) TarmedLeistungService.findFromCode("39.0015", null);
		VerrechenbarTarmedLeistung tlBaseRadiologyHospitalV = new VerrechenbarTarmedLeistung(tlBaseRadiologyHospital);

		IStatus status = tlBaseXRayV.add(consultation, patient, mandator);
		assertTrue(status.isOK());
		status = tlUltrasoundV.add(consultation, patient, mandator);
		assertFalse(status.isOK());
		status = tlBaseRadiologyHospitalV.add(consultation, patient, mandator);
		assertFalse(status.isOK());
	}

	@Test
	public void testIsCompatibleTarmedBilling() {
		TarmedOptifier optifier = new TarmedOptifier();

		TarmedLeistung tlBaseXRay = (TarmedLeistung) TarmedLeistungService.findFromCode("39.0020", null);
		TarmedLeistung tlUltrasound = (TarmedLeistung) TarmedLeistungService.findFromCode("39.3005", null);
		TarmedLeistung tlBaseRadiologyHospital = (TarmedLeistung) TarmedLeistungService.findFromCode("39.0015", null);
		TarmedLeistung tlBaseFirst5Min = (TarmedLeistung) TarmedLeistungService.findFromCode("00.0010", null);

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

}
