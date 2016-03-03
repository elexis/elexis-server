package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;

public class ArtikelstammItemServiceTest {

	@Test
	public void testGetImportSetCumulatedVersion()  {
		 int pharma = ArtikelstammItemService.INSTANCE.getCurrentVersion();
		 assertTrue("version is >0: "+pharma, pharma > 0);
	}
	
	@Test
	public void testFindAllEntries() {
		List<ArtikelstammItem> findAll = ArtikelstammItemService.INSTANCE.findAll(true);
		assertTrue(findAll.size()>0);
	}

}
