package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import org.junit.Test;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult;

public class LabResultServiceTest {

	@Test
	public void testGetLabResultById() {
		LabResult result = LabResultService.load("dafcc08ccd5e607301762").get();
		assertNotNull(result);
	}
	
}
