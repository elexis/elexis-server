package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Test;

import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Labor2009Tarif;

public class Labor2009TarifServiceTest {

	@Test
	public void testFindFromCodeString() {
		Optional<Labor2009Tarif> findFromCode = Labor2009TarifService.findFromCode("3358.00");
		assertTrue(findFromCode.isPresent());
		assertEquals("3.2.2, 5.1.3.2.10, 5.1.3.2.2, 5.1.3.2.5", findFromCode.get().getChapter());
		assertEquals(LocalDate.of(2015, 1, 1), findFromCode.get().getGueltigVon());
		assertNull(findFromCode.get().getGueltigBis());
		assertEquals("29", findFromCode.get().getTp());
	}

	@Test
	public void testFindFromCodeStringTimeTool() {
		Optional<Labor2009Tarif> findFromCode = Labor2009TarifService.findFromCode("1442.00", new TimeTool());
		assertTrue(findFromCode.isPresent());
		findFromCode = Labor2009TarifService.findFromCode("3358.00", new TimeTool());
		assertTrue(findFromCode.isPresent());
	}

}
