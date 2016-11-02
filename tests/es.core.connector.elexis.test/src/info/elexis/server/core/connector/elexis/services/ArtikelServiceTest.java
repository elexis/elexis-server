package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Test;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Artikel;

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


}
