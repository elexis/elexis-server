//package info.elexis.server.core.connector.elexis.services;
//
//import static org.junit.Assert.*;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import ch.elexis.core.constants.XidConstants;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.types.XidQuality;
//
//public class XidServiceTest extends AbstractServiceTest {
//
//	@Before
//	public void before() {
//		createTestMandantPatientFallBehandlung();
//	}
//
//	@After
//	public void after() {
//		cleanup();
//	}
//
//	@Test
//	public void testSetUnsetDomainId() {
//		String ahvNumber = "0815";
//		XidService.setDomainId(testContacts.get(0), XidConstants.CH_AHV, ahvNumber, XidQuality.ASSIGNMENT_REGIONAL);
//		XidService.setDomainId(testContacts.get(0), XidConstants.CH_AHV, ahvNumber, XidQuality.ASSIGNMENT_REGIONAL);
//
//		assertEquals(ahvNumber, XidService.getDomainId(testContacts.get(0), XidConstants.CH_AHV));
//		assertTrue(XidService.findByObjectAndDomain(testContacts.get(0), XidConstants.CH_AHV).isPresent());
//
//		XidService.unsetDomainId(testContacts.get(0), XidConstants.CH_AHV);
//		assertFalse(XidService.findByObjectAndDomain(testContacts.get(0), XidConstants.CH_AHV).isPresent());
//	}
//
//}
