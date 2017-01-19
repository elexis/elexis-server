package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;

public class ArtikelstammItemServiceTest {

	@Test
	public void testFindAllEntries() {
		List<? extends AbstractDBObjectIdDeleted> findAll = ArtikelstammItemService.findAll(ArtikelstammItem.class,
				true);
		assertTrue(findAll.size() > 0);
	}

	@Test
	public void testFindArtikelstammItemByGTIN() {
		Optional<ArtikelstammItem> blackBoxedItem = ArtikelstammItemService.findByGTIN("4032651091591");
		assertFalse(blackBoxedItem.isPresent());

		Optional<ArtikelstammItem> pharmaItem = ArtikelstammItemService.findByGTIN("7680475050118");
		assertTrue(pharmaItem.isPresent());
	}
}
