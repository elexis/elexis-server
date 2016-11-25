package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Test;

import ch.elexis.core.model.article.IArticle;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Artikel;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;

public class ArtikelServiceTest extends AbstractServiceTest {

	@Test
	public void testCreateEigenartikel() {
		Artikel ea1 = ArtikelService.INSTANCE.create("Name", "InternalName", Artikel.TYP_EIGENARTIKEL);

		ArtikelService.INSTANCE.write(ea1);

		Optional<Artikel> findById = ArtikelService.INSTANCE.findById(ea1.getId());
		assertTrue(findById.isPresent());
		assertEquals(ea1.getName(), findById.get().getName());
		assertEquals(ea1.getNameIntern(), findById.get().getNameIntern());
		assertEquals(ea1.getTyp(), findById.get().getTyp());

		ArtikelService.INSTANCE.remove(ea1);
	}

	@Test
	public void testFindAnyArticleByGTIN() {
		Artikel ea3 = ArtikelService.INSTANCE.create("Name3", "InternalName3", Artikel.TYP_EIGENARTIKEL);
		ea3.setEan("1234567890123");
		ArtikelService.INSTANCE.write(ea3);

		Optional<? extends IArticle> findByGTIN = new ArticleService().findAnyByGTIN("7680475040157");
		assertTrue(findByGTIN.isPresent());
		assertTrue(findByGTIN.get() instanceof ArtikelstammItem);

		findByGTIN = new ArticleService().findAnyByGTIN("1234567890123");
		assertTrue(findByGTIN.isPresent());
		assertTrue(findByGTIN.get() instanceof Artikel);
	}

}
