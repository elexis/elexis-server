//package info.elexis.server.core.connector.elexis.services;
//
//import static org.junit.Assert.*;
//
//import java.time.LocalDate;
//
//import org.exparity.hamcrest.date.LocalDateMatchers;
//import org.hamcrest.MatcherAssert;
//import org.junit.Test;
//
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.PhysioLeistung;
//
//public class PhysioLeistungTest extends AbstractServiceTest {
//
//	@Test
//	public void testLoadVerifyValuesPhysioLeistung() {
//		PhysioLeistung load = PhysioLeistungService.load("facb45d540ec3bfe604113").get();
//		MatcherAssert.assertThat(load.getValidFrom(), LocalDateMatchers.sameDay(LocalDate.of(1970, 1, 1)));
//		MatcherAssert.assertThat(load.getValidUntil(), LocalDateMatchers.sameDay(LocalDate.of(2038, 1, 18)));
//		assertEquals("1900", load.getTp());
//		assertEquals("7352", load.getZiffer());
//		assertEquals("Zuschlagsposition für die Benutzung des Gehbads / Schwimmbads", load.getTitel());
//		assertNull(load.getDescription());
//	}
//
//}
