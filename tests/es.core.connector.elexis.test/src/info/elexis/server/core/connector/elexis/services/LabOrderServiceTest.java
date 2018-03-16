package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabOrder;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult;

public class LabOrderServiceTest {

	LabResult labresult;

	@Before
	public void before() {
		labresult = LabResultService.load("ved209fdb6b421a56077772").get();
		assertEquals("101", labresult.getResult());
		assertNotNull(labresult);
		
	}

	@Test
	public void testFindLabOrderByLabResult() {
		Optional<LabOrder> labOrder = LabOrderService.findLabOrderByLabResult(labresult);
		assertTrue(labOrder.isPresent());
	}

}
