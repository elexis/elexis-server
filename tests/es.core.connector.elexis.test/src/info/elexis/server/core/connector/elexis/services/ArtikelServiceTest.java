package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Artikel;

public class ArtikelServiceTest extends AbstractServiceTest {

	@Test
	public void testCreateEigenartikel() {
		Artikel ea1 = ArtikelService.INSTANCE.create("Name", "InternalName", Artikel.TYP_EIGENARTIKEL);

		Optional<Artikel> findById = ArtikelService.INSTANCE.findById(ea1.getId());
		assertTrue(findById.isPresent());
		assertEquals(ea1.getName(), findById.get().getName());
		assertEquals(ea1.getNameIntern(), findById.get().getNameIntern());
		assertEquals(ea1.getTyp(), findById.get().getTyp());

		ArtikelService.INSTANCE.remove(ea1);
	}

	@Test
	public void testFindStockArticles() {
		Artikel ea1 = ArtikelService.INSTANCE.create("Name", "InternalName", Artikel.TYP_EIGENARTIKEL);
		Artikel ea2 = ArtikelService.INSTANCE.create("Name2", "InternalName2", Artikel.TYP_EIGENARTIKEL);
		Artikel ea3 = ArtikelService.INSTANCE.create("Name3", "InternalName3", Artikel.TYP_EIGENARTIKEL);
		Artikel ea4 = ArtikelService.INSTANCE.create("Name4", "InternalName4", Artikel.TYP_EIGENARTIKEL);

		ea1.setIstbestand(1);
		ea2.setMinbestand(3);
		ea3.setMaxbestand(15);
		ArtikelService.INSTANCE.flush();

		// insert deliberately wrong data to check converter
		ArtikelService.INSTANCE.em.getTransaction().begin();
		ArtikelService.INSTANCE.em
				.createNativeQuery("UPDATE artikel SET ISTBESTAND = ' 5 ' WHERE ID = '" + ea3.getId() + "'")
				.executeUpdate();
		ArtikelService.INSTANCE.em.getTransaction().commit();

		List<Artikel> allStockArticles = ArtikelService.getAllStockArticles(Artikel.TYP_EIGENARTIKEL);
		assertEquals(3, allStockArticles.size());

		ArtikelService.INSTANCE.remove(ea1);
		ArtikelService.INSTANCE.remove(ea2);
		ArtikelService.INSTANCE.remove(ea3);
		ArtikelService.INSTANCE.remove(ea4);
	}

}
