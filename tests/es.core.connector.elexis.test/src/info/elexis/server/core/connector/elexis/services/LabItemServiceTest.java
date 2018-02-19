package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.elexis.core.types.LabItemTyp;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabItem;

public class LabItemServiceTest {

	@Test
	public void testCreateLabItem() {
		Kontakt labKontakt = new KontaktService.OrganizationBuilder("Test-Laboratory").laboratory().buildAndSave();
		LabItem labItem = new LabItemService.Builder("TST", "Test", labKontakt, "5-10", "5-12", "nmol/l",
				LabItemTyp.NUMERIC, "T", 1).buildAndSave();

		assertEquals("TST", labItem.getCode());
		assertEquals("Test", labItem.getName());
		assertEquals("5-10", labItem.getReferenceMale());
		assertEquals("5-12", labItem.getReferenceFemale());
		assertEquals("nmol/l", labItem.getUnit());
		assertEquals(LabItemTyp.NUMERIC, labItem.getTyp());
		assertEquals("T", labItem.getGroup());
		assertTrue(labItem.isVisible());
		assertFalse(labItem.isDeleted());
	}

}
