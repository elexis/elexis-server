package info.elexis.server.core.connector.elexis.billable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.base.ch.ticode.TessinerCode;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.status.ObjectStatus;
import ch.elexis.core.types.Gender;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.billable.optifier.TarmedOptifier;
import info.elexis.server.core.connector.elexis.jpa.ElexisTypeMap;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Diagnosis;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung.MandantType;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
import info.elexis.server.core.connector.elexis.services.BehandlungService;
import info.elexis.server.core.connector.elexis.services.ConfigService;
import info.elexis.server.core.connector.elexis.services.FallService;
import info.elexis.server.core.connector.elexis.services.JPAQuery;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;
import info.elexis.server.core.connector.elexis.services.KontaktService;
import info.elexis.server.core.connector.elexis.services.TarmedLeistungService;
import info.elexis.server.core.connector.elexis.services.VerrechnetService;
import info.elexis.server.core.connector.elexis.services.util.MultiplikatorList;

public class TarmedOptifierTest {

	private static Kontakt userContact;
	private static Kontakt mandator;
	private static TarmedOptifier optifier;
	private static Kontakt patGrissemann, patStermann, patOneYear;
	private static Behandlung konsGriss, konsSter, konsOneYear;
	private static IBillable<TarmedLeistung> tlBaseFirst5Min, tlBaseXRay, tlBaseRadiologyHospital, tlUltrasound,
			tlTapingCat1, tlAgeTo1Month, tlAgeTo7Years, tlAgeFrom7Years, tlGroupLimit1, tlGroupLimit2;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		optifier = new TarmedOptifier();

		importTarmedReferenceData();

		JPAQuery<Kontakt> mandantQuery = new JPAQuery<Kontakt>(Kontakt.class);
		mandantQuery.add(Kontakt_.person, QUERY.EQUALS, true);
		mandantQuery.add(Kontakt_.mandator, QUERY.EQUALS, true);
		List<Kontakt> mandants = mandantQuery.execute();
		assertTrue(!mandants.isEmpty());
		mandator = mandants.get(0);
		userContact = mandator;

		// init some basic services
		tlBaseFirst5Min = new VerrechenbarTarmedLeistung(
				TarmedLeistungService.findFromCode("00.0010", new TimeTool()).get());
		tlBaseXRay = new VerrechenbarTarmedLeistung(
				TarmedLeistungService.findFromCode("39.0020", new TimeTool()).get());
		tlBaseRadiologyHospital = new VerrechenbarTarmedLeistung(
				TarmedLeistungService.findFromCode("39.0015", new TimeTool()).get());
		tlUltrasound = new VerrechenbarTarmedLeistung(
				TarmedLeistungService.findFromCode("39.3005", new TimeTool()).get());
		tlTapingCat1 = new VerrechenbarTarmedLeistung(
				TarmedLeistungService.findFromCode("01.0110", new TimeTool()).get());

		tlAgeTo1Month = new VerrechenbarTarmedLeistung(
				TarmedLeistungService.findFromCode("00.0870", new TimeTool()).get());
		tlAgeTo7Years = new VerrechenbarTarmedLeistung(
				TarmedLeistungService.findFromCode("00.0900", new TimeTool()).get());
		tlAgeFrom7Years = new VerrechenbarTarmedLeistung(
				TarmedLeistungService.findFromCode("00.0890", new TimeTool()).get());

		tlGroupLimit1 = new VerrechenbarTarmedLeistung(
				TarmedLeistungService.findFromCode("02.0310", new TimeTool()).get());
		tlGroupLimit2 = new VerrechenbarTarmedLeistung(
				TarmedLeistungService.findFromCode("02.0340", new TimeTool()).get());

		// Patient Grissemann with case and consultation
		patGrissemann = new KontaktService.PersonBuilder("Grissemann", "Christoph", LocalDate.of(1966, 5, 17),
				Gender.MALE).patient().buildAndSave();
		Fall fallGriss = new FallService.Builder(patGrissemann, "Testfall Grissemann",
				Preferences.USR_DEFCASEREASON_DEFAULT, "KVG").buildAndSave();
		fallGriss.setKostentrKontakt(patGrissemann);
		// fallGriss.setInfoElement("Kostenträger", patGrissemann.getId());
		konsGriss = new BehandlungService.Builder(fallGriss, mandator).buildAndSave();
		resetKons(konsGriss);

		// Patient Stermann with case and consultation
		patStermann = new KontaktService.PersonBuilder("Stermann", "Dirk", LocalDate.of(1965, 12, 7), Gender.MALE)
				.patient().buildAndSave();
		Fall fallSter = new FallService.Builder(patStermann, "Testfall Stermann", Preferences.USR_DEFCASEREASON_DEFAULT,
				"KVG").buildAndSave();
		// fallSter.setInfoElement("Kostenträger", patStermann.getId());
		konsSter = new BehandlungService.Builder(fallSter, mandator).buildAndSave();
		resetKons(konsSter);

		// Patient OneYear with case and consultation
		patOneYear = new KontaktService.PersonBuilder("One", "Year", LocalDate.now().minusYears(1).minusDays(1),
				Gender.MALE).patient().buildAndSave();
		Fall fallOneYear = new FallService.Builder(patOneYear, "Testfall One", Preferences.USR_DEFCASEREASON_DEFAULT,
				FallService.getAbrechnungsSysteme()[0]).buildAndSave();
		// fallSter.setInfoElement("Kostenträger", patOneYear.getId());
		konsOneYear = new BehandlungService.Builder(fallOneYear, mandator).buildAndSave();
		resetKons(konsOneYear);

	}

	private static void importTarmedReferenceData() throws FileNotFoundException {
		// Importer not provided we import the raw db; set values that would have
		// been set by the importer
		ConfigService.INSTANCE
				.setFromBoolean("ch.elexis.data.importer.TarmedReferenceDataImporter/referenceinfoavailable", true);
	}

	private IBillable<TarmedLeistung> additionalService;
	private IBillable<TarmedLeistung> mainService;

	@Test
	public void testAddCompatibleAndIncompatible() {
		clearKons(konsGriss);
		IStatus resultGriss = optifier.add(tlUltrasound, konsGriss);
		assertTrue(resultGriss.isOK());
		resultGriss = optifier.add(tlBaseXRay, konsGriss);
		assertFalse(resultGriss.isOK());
		resultGriss = optifier.add(tlTapingCat1, konsGriss);
		assertFalse(resultGriss.isOK());
		resultGriss = optifier.add(TarmedLeistungService.getVerrechenbarFromCode("39.3830", new TimeTool(), null).get(),
				konsGriss);
		assertTrue(resultGriss.isOK());
		resetKons(konsGriss);
	}

	@Test
	public void testAddMultipleIncompatible() {
		clearKons(konsSter);
		IStatus resultSter = optifier.add(tlBaseXRay, konsSter);
		assertTrue(resultSter.getMessage(), resultSter.isOK());
		resultSter = optifier.add(tlUltrasound, konsSter);
		assertFalse(resultSter.isOK());
		resultSter = optifier.add(tlBaseRadiologyHospital, konsSter);
		assertFalse(resultSter.isOK());
	}

	@Test
	public void testIsCompatible() {
		IStatus resCompatible = optifier.isCompatible(tlBaseXRay.getEntity(), tlUltrasound.getEntity(), konsSter);
		assertFalse(resCompatible.isOK());
		String resText = "";
		if (!resCompatible.getMessage().isEmpty()) {
			resText = resCompatible.getMessage();
		}
		assertEquals("39.3005 nicht kombinierbar mit Kapitel 39.01", resText);
		resCompatible = optifier.isCompatible(tlUltrasound.getEntity(), tlBaseXRay.getEntity(), konsSter);
		assertTrue(resCompatible.isOK());

		resCompatible = optifier.isCompatible(tlBaseXRay.getEntity(), tlBaseRadiologyHospital.getEntity(), konsSter);
		assertFalse(resCompatible.isOK());
		if (!resCompatible.getMessage().isEmpty()) {
			resText = resCompatible.getMessage();
		}
		assertEquals("39.0015 nicht kombinierbar mit Leistung 39.0020", resText);

		resCompatible = optifier.isCompatible(tlBaseRadiologyHospital.getEntity(), tlUltrasound.getEntity(), konsSter);
		assertFalse(resCompatible.isOK());

		resCompatible = optifier.isCompatible(tlBaseXRay.getEntity(), tlBaseFirst5Min.getEntity(), konsSter);
		assertTrue(resCompatible.isOK());

		resCompatible = optifier.isCompatible(tlBaseFirst5Min.getEntity(), tlBaseRadiologyHospital.getEntity(),
				konsSter);
		assertTrue(resCompatible.isOK());

		clearKons(konsSter);
		resCompatible = optifier.isCompatible(
				(TarmedLeistung) TarmedLeistungService.findFromCode("00.0010", new TimeTool(), null).get(),
				(TarmedLeistung) TarmedLeistungService.findFromCode("00.1345", new TimeTool(), null).get(), konsSter);
		assertFalse(resCompatible.isOK());
		resText = "";
		if (!resCompatible.getMessage().isEmpty()) {
			resText = resCompatible.getMessage();
		}
		assertEquals("00.1345 nicht kombinierbar mit 00.0010, wegen Block Kumulation", resText);

		resCompatible = optifier.isCompatible(
				(TarmedLeistung) TarmedLeistungService.findFromCode("01.0265", new TimeTool(), null).get(),
				(TarmedLeistung) TarmedLeistungService.findFromCode("00.1345", new TimeTool(), null).get(), konsSter);
		assertTrue(resCompatible.isOK());

		resCompatible = optifier.isCompatible(
				(TarmedLeistung) TarmedLeistungService.findFromCode("00.0510", new TimeTool(), null).get(),
				(TarmedLeistung) TarmedLeistungService.findFromCode("03.0020", new TimeTool(), null).get(), konsSter);
		assertFalse(resCompatible.isOK());
		resText = "";
		if (!resCompatible.getMessage().isEmpty()) {
			resText = resCompatible.getMessage();
		}
		assertEquals("03.0020 nicht kombinierbar mit 00.0510, wegen Block Kumulation", resText);

		resCompatible = optifier.isCompatible(
				(TarmedLeistung) TarmedLeistungService.findFromCode("00.2510", new TimeTool(), null).get(),
				(TarmedLeistung) TarmedLeistungService.findFromCode("03.0020", new TimeTool(), null).get(), konsSter);
		assertTrue(resCompatible.isOK());

		resetKons(konsSter);
	}

	@Test
	public void testSetBezug() {
		clearKons(konsSter);

		additionalService = new VerrechenbarTarmedLeistung(
				TarmedLeistungService.findFromCode("39.5010", new TimeTool()).get());
		mainService = new VerrechenbarTarmedLeistung(
				TarmedLeistungService.findFromCode("39.5060", new TimeTool()).get());
		// additional without main, not allowed
		IStatus resultSter = optifier.add(additionalService, konsSter);
		assertFalse(resultSter.getMessage(), resultSter.isOK());
		// additional after main, allowed
		resultSter = optifier.add(mainService, konsSter);
		assertTrue(resultSter.isOK());
		assertTrue(getVerrechnet(konsSter, mainService).isPresent());

		resultSter = optifier.add(additionalService, konsSter);
		assertTrue(resultSter.isOK());
		assertTrue(getVerrechnet(konsSter, additionalService).isPresent());
		assertEquals(1, VerrechnetService.getVerrechnetForBehandlung(konsSter, additionalService).get().getZahl());

		// another additional, not allowed
		resultSter = optifier.add(additionalService, konsSter);
		assertFalse(resultSter.isOK());
		assertTrue(getVerrechnet(konsSter, additionalService).isPresent());

		// remove, and add again
		Optional<Verrechnet> verrechnet = getVerrechnet(konsSter, additionalService);
		assertTrue(verrechnet.isPresent());
		IStatus result = optifier.remove(verrechnet.get());
		assertTrue(result.isOK());
		assertFalse(VerrechnetService.getVerrechnetForBehandlung(konsSter, additionalService).isPresent());
		resultSter = optifier.add(additionalService, konsSter);
		assertTrue(resultSter.isOK());
		assertEquals(1, VerrechnetService.getVerrechnetForBehandlung(konsSter, additionalService).get().getZahl());
		
		// add another main and additional
		resultSter = optifier.add(mainService, konsSter);
		assertTrue(resultSter.isOK());
		assertTrue(getVerrechnet(konsSter, mainService).isPresent());
		assertEquals(2, VerrechnetService.getVerrechnetForBehandlung(konsSter, mainService).get().getZahl());
		resultSter = optifier.add(additionalService, konsSter);
		assertTrue(resultSter.getMessage(), resultSter.isOK());
		assertTrue(getVerrechnet(konsSter, additionalService).isPresent());
		assertEquals(2, VerrechnetService.getVerrechnetForBehandlung(konsSter, additionalService).get().getZahl());

		// remove main service, should also remove additional service
		verrechnet = getVerrechnet(konsSter, mainService);
		result = optifier.remove(verrechnet.get());
		assertTrue(result.isOK());
		assertFalse(getVerrechnet(konsSter, mainService).isPresent());
		assertFalse(getVerrechnet(konsSter, additionalService).isPresent());

		resetKons(konsSter);
	}

	@Test
	public void testOneYear() {
		// additional without main, not allowed
		IStatus result = optifier.add(tlAgeTo1Month, konsOneYear);
		assertFalse(result.isOK());

		result = optifier.add(tlAgeTo7Years, konsOneYear);
		assertTrue(result.isOK());

		result = optifier.add(tlAgeFrom7Years, konsOneYear);
		assertFalse(result.isOK());
	}

	@Test
	public void testGroupLimitation() {
		// limit on group 31 is 48 times per week
		resetKons(konsGriss);
		for (int i = 0; i < 24; i++) {
			ObjectStatus result = (ObjectStatus) BehandlungService.chargeBillableOnBehandlung(konsGriss, tlGroupLimit1);
			assertTrue(result.isOK());
		}
		resetKons(konsSter);
		for (int i = 0; i < 24; i++) {
			IStatus result = BehandlungService.chargeBillableOnBehandlung(konsSter, tlGroupLimit2);
			assertTrue(result.isOK());
		}

		IStatus result = BehandlungService.chargeBillableOnBehandlung(konsGriss, tlGroupLimit2);
		assertTrue(result.isOK());

		result = BehandlungService.chargeBillableOnBehandlung(konsSter, tlGroupLimit1);
		assertTrue(result.getMessage(), result.isOK());

		for (int i = 0; i < 23; i++) {
			result = BehandlungService.chargeBillableOnBehandlung(konsGriss, tlGroupLimit1);
			assertTrue(result.isOK());
		}
		for (int i = 0; i < 23; i++) {
			result = BehandlungService.chargeBillableOnBehandlung(konsSter, tlGroupLimit2);
			assertTrue(result.isOK());
		}

		assertEquals(47, VerrechnetService.getVerrechnetForBehandlung(konsGriss, tlGroupLimit1).get().getZahl());
		assertEquals(1, VerrechnetService.getVerrechnetForBehandlung(konsGriss, tlGroupLimit2).get().getZahl());

		result = BehandlungService.chargeBillableOnBehandlung(konsGriss, tlGroupLimit2);
		assertFalse(result.getMessage(), result.isOK());

		result = BehandlungService.chargeBillableOnBehandlung(konsSter, tlGroupLimit1);
		assertFalse(result.isOK());

		resetKons(konsGriss);
		resetKons(konsSter);
	}

	private static void resetKons(Behandlung kons) {
		clearKons(kons);
		TessinerCode tc = TessinerCode.getFromCode("T1");
		Diagnosis diagnosis = new Diagnosis();
		diagnosis.setCode(tc.getCode());
		diagnosis.setText(tc.getText());
		diagnosis.setDiagnosisClass(ElexisTypeMap.TYPE_TESSINER_CODE);
		BehandlungService.setDiagnosisOnConsultation(kons, diagnosis);
		BehandlungService.chargeBillableOnBehandlung(kons, tlBaseFirst5Min, kons.getMandant(), kons.getMandant());
	}

	@Test
	public void testDignitaet() {
		Behandlung kons = konsGriss;
		setUpDignitaet(kons);

		// default mandant type is specialist
		clearKons(kons);
		ObjectStatus result = (ObjectStatus) BehandlungService.chargeBillableOnBehandlung(kons, tlBaseFirst5Min);
		assertTrue(result.isOK());
		Verrechnet verrechnet = (Verrechnet) result.getObject();
		assertNotNull(verrechnet);
		int amountAL = VerrechnetService.getAL(verrechnet);
		assertEquals(1042, amountAL);
		Money amount = VerrechnetService.getNettoPreis(verrechnet);
		assertEquals(15.45, amount.getAmount(), 0.01);

		// set the mandant type to practitioner
		clearKons(kons);
		TarmedLeistungService.setMandantType(kons.getMandant(), MandantType.PRACTITIONER);
		result = (ObjectStatus) BehandlungService.chargeBillableOnBehandlung(kons, tlBaseFirst5Min);
		assertTrue(result.isOK());
		verrechnet = (Verrechnet) result.getObject();
		assertNotNull(verrechnet);
		amountAL = VerrechnetService.getAL(verrechnet);
		assertEquals(969, amountAL);
		amount = VerrechnetService.getNettoPreis(verrechnet);
		assertEquals(14.84, amount.getAmount(), 0.01);

		tearDownDignitaet(kons);

		// set the mandant type to practitioner
		clearKons(kons);
		TarmedLeistungService.setMandantType(kons.getMandant(), MandantType.SPECIALIST);
		result = (ObjectStatus) BehandlungService.chargeBillableOnBehandlung(kons, tlBaseFirst5Min);
		assertTrue(result.isOK());
		verrechnet = (Verrechnet) result.getObject();
		assertNotNull(verrechnet);
		amountAL = VerrechnetService.getAL(verrechnet);
		assertEquals(957, amountAL);
		amount = VerrechnetService.getNettoPreis(verrechnet);
		assertEquals(17.76, amount.getAmount(), 0.01);
	}

	private void setUpDignitaet(Behandlung kons) {
		Map<String, String> extension = tlBaseFirst5Min.getEntity().getExtension().getLimits();
		// set reduce factor
		extension.put(TarmedLeistung.EXT_FLD_F_AL_R, "0.93");
		// the AL value
		extension.put(TarmedLeistung.EXT_FLD_TP_AL, "10.42");
		// add additional multiplier
		LocalDate yesterday = LocalDate.now().minus(1, ChronoUnit.DAYS);
		MultiplikatorList multis = new MultiplikatorList("VK_PREISE", FallService.getAbrechnungsSystem(kons.getFall()));
		multis.insertMultiplikator(new TimeTool(yesterday), "0.83");

		tlBaseFirst5Min.getEntity().getExtension().setLimits(extension);
	}

	private void tearDownDignitaet(Behandlung kons) {
		Map<String, String> extension = tlBaseFirst5Min.getEntity().getExtension().getLimits();
		// clear reduce factor
		extension = tlBaseFirst5Min.getEntity().getExtension().getLimits();
		extension.remove(TarmedLeistung.EXT_FLD_F_AL_R);
		// reset AL value
		extension.put(TarmedLeistung.EXT_FLD_TP_AL, "9.57");
		// remove additional multiplier
		LocalDate yesterday = LocalDate.now().minus(1, ChronoUnit.DAYS);
		MultiplikatorList multis = new MultiplikatorList("VK_PREISE", FallService.getAbrechnungsSystem(kons.getFall()));
		multis.removeMultiplikator(new TimeTool(yesterday), "0.83");

		tlBaseFirst5Min.getEntity().getExtension().setLimits(extension);
	}

	private static void clearKons(Behandlung kons) {
		for (Verrechnet verrechnet : VerrechnetService.getAllVerrechnetForBehandlung(kons)) {
			VerrechnetService.remove(verrechnet);
		}
	}

	private Optional<Verrechnet> getVerrechnet(Behandlung kons, IBillable<TarmedLeistung> leistung) {
		for (Verrechnet verrechnet : VerrechnetService.getAllVerrechnetForBehandlung(kons)) {
			if (VerrechnetService.getVerrechenbar(verrechnet).get().getCode().equals(leistung.getEntity().getCode())) {
				return Optional.of(verrechnet);
			}
		}
		return Optional.empty();
	}
}
