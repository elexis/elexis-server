//package info.elexis.server.core.connector.elexis.services;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertTrue;
//
//import java.util.Optional;
//
//import org.junit.Test;
//
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Leistungsblock;
//
//public class LeistungsblockServiceTest extends AbstractServiceTest {
//
//	@Test
//	public void testLoadLeistungsblockAllMandators() {
//		Optional<Leistungsblock> load = LeistungsblockService.load("V535469d51e5785a20985");
//		assertTrue(load.isPresent());
//		assertEquals("b7", load.get().getName());
//		assertEquals("b7", load.get().getMacro());
//		assertNull(load.get().getMandator());
//		assertEquals(7, load.get().getServices().size());
//		assertTrue(load.get().getServices().contains(TarmedLeistungService.load("00.0060-20120601").get()));
//		assertTrue(load.get().getServices().contains(TarmedLeistungService.load("00.0070-20010101").get()));
//		assertTrue(load.get().getServices().contains(TarmedLeistungService.load("00.0080-20010101").get()));
//	}
//
//	@Test
//	public void testLoadLeistungsblockSingleMandator() {
//		Optional<Leistungsblock> load = LeistungsblockService.load("g5b231c378348591701614");
//		assertTrue(load.isPresent());
//		assertEquals("b8", load.get().getName());
//		assertEquals("b8", load.get().getMacro());
//		assertEquals(KontaktService.load("h2c1172107ce2df95065").get(), load.get().getMandator());
//		assertEquals(8, load.get().getServices().size());
//		assertTrue(load.get().getServices().contains(TarmedLeistungService.load("00.0060-20120601").get()));
//		assertTrue(load.get().getServices().contains(TarmedLeistungService.load("00.0070-20010101").get()));
//		assertTrue(load.get().getServices().contains(TarmedLeistungService.load("00.0080-20010101").get()));
//	}
//
//}
