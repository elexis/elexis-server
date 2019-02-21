//package info.elexis.server.core.connector.elexis.services;
//
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedExtension;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;
//
//import static org.junit.Assert.*;
//
//import java.time.LocalDate;
//import java.util.Map;
//
//import org.junit.Test;
//
//public class TarmedLeistungServiceTest extends AbstractServiceTest {
//

//
//	@Test
//	public void testGetMinutesForTarmedLeistung() {
//		TarmedLeistung code = TarmedLeistungService.findFromCode("00.0010", null).get();
//		int minutesForTarmedLeistung = TarmedLeistungService.getMinutesForTarmedLeistung(code);
//		assertEquals(5, minutesForTarmedLeistung);
//	}
//
//	@Test
//	public void testGetExclusions() {
//		TarmedLeistung code = TarmedLeistungService.findFromCode("39.0015", null).get();
//		String exclusionsForTarmedLeistung = TarmedLeistungService.getExclusionsForTarmedLeistung(code,
//				LocalDate.now());
//		assertTrue(exclusionsForTarmedLeistung.contains("39.0021"));
//		assertTrue(exclusionsForTarmedLeistung.contains("39.0016"));
//		assertTrue(exclusionsForTarmedLeistung.contains("39.0020"));
//	}
//
//}
