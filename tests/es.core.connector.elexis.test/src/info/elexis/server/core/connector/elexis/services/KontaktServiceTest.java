package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
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
	
	@Test
	public void testFindByIdAndListFaelle() {
		Kontakt cont = KontaktService.INSTANCE.findById("ab692057d60c01b62016460");
		List<Fall> faelle = cont.getFaelle();
		assertTrue("Found more than one fall", faelle.size()>1);
		Set<String> xids = cont.getXids().keySet();
		assertTrue("Found at least one Xid", xids.size()>0);
	}
}
