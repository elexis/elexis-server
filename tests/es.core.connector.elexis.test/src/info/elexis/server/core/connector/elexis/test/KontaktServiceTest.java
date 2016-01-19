package info.elexis.server.core.connector.elexis.test;

import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.KontaktService;

public class KontaktServiceTest {
	
	@Test
	public void testCreateAndDeleteKontakt() throws InstantiationException, IllegalAccessException {
		Kontakt val = KontaktService.INSTANCE.create();
		Kontakt findById = KontaktService.INSTANCE.findById(val.getId());
		assertEquals(val.getId(), findById.getId());
		KontaktService.INSTANCE.remove(val);	
		Kontakt found = KontaktService.INSTANCE.findById(val.getId());
		assertNull(found);
	}
	
	@Test
	public void testFindByIdStartingWith()  {
		 List<Kontakt> result = KontaktService.INSTANCE.findByIdStartingWith("A");
		 assertTrue("Found more than 2 contacts", result.size()>2);
	}
}
