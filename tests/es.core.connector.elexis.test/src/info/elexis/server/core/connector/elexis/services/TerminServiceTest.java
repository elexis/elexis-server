package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Termin;

public class TerminServiceTest {

	@Test
	public void testFindAllAppointments() {
		List<Termin> all = TerminService.findAll(false);
		assertEquals(21, all.size());
		List<Termin> allAppointments = TerminService.findAllAppointments();
		assertEquals(20, allAppointments.size());
	}

	@Test
	public void testFindAllUsedAppointmentAreas() {
		List<String> areas = TerminService.findAllUsedAppointmentAreas();
		assertTrue(areas.size() > 3);
	}

}
