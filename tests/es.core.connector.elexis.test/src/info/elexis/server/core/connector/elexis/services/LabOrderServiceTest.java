package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabOrder;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult;

public class LabOrderServiceTest {

	LabResult labresult;

	@Before
	public void before() {
		labresult = LabResultService.INSTANCE.findById("h2458737678162e60665").get();
		assertNotNull(labresult);
	}

	@Test
	public void testFindLabOrderByLabResult() {
		Optional<LabOrder> labOrder = LabOrderService.findLabOrderByLabResult(labresult);
		assertTrue(labOrder.isPresent());
	}

	@Test
	public void testFindAllLabOrdersInSameOrderIdGroup() {
		Optional<LabOrder> labOrder = LabOrderService.findLabOrderByLabResult(labresult);
		assertTrue(labOrder.isPresent());
		List<LabOrder> laborderGroup = LabOrderService.INSTANCE.findAllLabOrdersInSameOrderIdGroup(labOrder.get());
		assertTrue(laborderGroup.size() > 0);
	}

}
