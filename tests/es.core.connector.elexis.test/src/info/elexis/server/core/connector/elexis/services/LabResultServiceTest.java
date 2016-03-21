package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import org.junit.Test;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult;

public class LabResultServiceTest {

	@Test
	public void testGetLabResultById() {
		LabResult result = LabResultService.INSTANCE.findById("h2458737678162e60665").get();
		assertNotNull(result);
	}
	
}
