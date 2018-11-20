package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class TerminServiceTest {

	@Test
	public void testFindAllUsedAppointmentAreas() {
		List<String> areas = TerminService.findAllUsedAppointmentAreas();
		assertTrue(areas.size() > 3);
	}

}
