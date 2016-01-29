package info.elexis.server.core.connector.elexis.test;

import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Faelle;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Xid;
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
	
	@Test
	public void testFindByIdAndListFaelle() {
		Kontakt cont = KontaktService.INSTANCE.findById("ab692057d60c01b62016460");
		List<Faelle> faelle = cont.getFaelle();
		assertTrue("Found more than one fall", faelle.size()>1);
		List<Xid> xids = cont.getXids();
		assertTrue("Found at least one Xid", xids.size()>0);
	}
}
