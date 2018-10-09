//package info.elexis.server.core.connector.elexis.services;
//
//import static org.junit.Assert.*;
//
//import java.util.Optional;
//
//import org.junit.Test;
//
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Eigenleistung;
//
//public class EigenleistungServiceTest {
//
//	@Test
//	public void testCreateAndDeleteEigenleistung() throws InstantiationException, IllegalAccessException {
//		Eigenleistung el = new EigenleistungService.Builder("399999").buildAndSave();
//		Optional<? extends AbstractDBObjectIdDeleted> findByCode = EigenleistungService.findByCode("399999");
//		assertTrue(findByCode.isPresent());
//		EigenleistungService.remove(el);
//		Optional<? extends AbstractDBObjectIdDeleted> findByCode2 = EigenleistungService.findByCode("399999");
//		assertTrue(!findByCode2.isPresent());
//	}
//
//}
