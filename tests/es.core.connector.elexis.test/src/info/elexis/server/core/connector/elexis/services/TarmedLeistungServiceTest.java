package info.elexis.server.core.connector.elexis.services;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedExtension;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.Map;

import org.junit.Test;

public class TarmedLeistungServiceTest extends AbstractServiceTest {

	@Test
	public void testGetFromCode() {
		TarmedLeistung code = TarmedLeistungService.findFromCode("00.0010", null).get();
		assertNotNull(code.getExtension());
		assertEquals("00.0010-20010101", code.getId());
		assertEquals("FMH05", code.getDigniQuanti());
		assertEquals("9999", code.getDigniQuali());
		assertEquals("0001", code.getSparte());
		assertEquals(LocalDate.of(2001, 1, 1), code.getGueltigVon());
		assertEquals(LocalDate.of(2199, 12, 31), code.getGueltigBis());
		assertEquals("00.0010", code.getCode());

		TarmedExtension extension = code.getExtension();
		assertNotNull(extension.getLimits());
		Map<String, String> limits = extension.getLimits();

		assertEquals("5.0", limits.get("LSTGIMES_MIN"));
		assertEquals("0.0", limits.get("ANZ_ASSI"));
		assertEquals("0.0", limits.get("WECHSEL_MIN"));
		assertEquals("0", limits.get("SEITE"));
		assertEquals("01", limits.get("K_PFL"));
		assertEquals("1.0", limits.get("F_AL"));
		assertEquals("0.0", limits.get("BEFUND_MIN"));
		assertEquals("9.57", limits.get("TP_AL"));
		assertEquals("0.0", limits.get("VBNB_MIN"));
		assertEquals("0.0", limits.get("TP_ASSI"));
		assertEquals("N", limits.get("BEHANDLUNGSART"));
		assertEquals("1.0", limits.get("F_TL"));
		assertEquals("H", limits.get("LEISTUNG_TYP"));
		assertEquals("8.19", limits.get("TP_TL"));

	}

	@Test
	public void testGetMinutesForTarmedLeistung() {
		TarmedLeistung code = TarmedLeistungService.findFromCode("00.0010", null).get();
		int minutesForTarmedLeistung = TarmedLeistungService.getMinutesForTarmedLeistung(code);
		assertEquals(5, minutesForTarmedLeistung);
	}

	@Test
	public void testGetExclusions() {
		TarmedLeistung code = TarmedLeistungService.findFromCode("39.0015", null).get();
		String exclusionsForTarmedLeistung = TarmedLeistungService.getExclusionsForTarmedLeistung(code,
				LocalDate.now());
		assertTrue(exclusionsForTarmedLeistung.contains("39.0021"));
		assertTrue(exclusionsForTarmedLeistung.contains("39.0016"));
		assertTrue(exclusionsForTarmedLeistung.contains("39.0020"));
	}

}
