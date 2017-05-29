package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Artikel;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Sticker;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.StickerClassLink;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.StickerObjectLink;

public class StickerServiceTest extends AbstractServiceTest {

	@Test
	public void testCreateAndUpdateSticker(){
		//creates a sticker
		Sticker sticker = new Sticker();
		sticker.setBackground("123456");
		sticker.setForeground("123456");
		sticker.setName("Sticker 1");
		
		sticker = StickerService.save(sticker);

		assertEquals(1, StickerService.findAll(Sticker.class, false).size());
		assertEquals("Sticker 1", sticker.getName());
		assertEquals(0, sticker.getStickerClassLinks().size());
		
		// updates a sticker
		sticker.setName("Sticker 11");
		sticker.setBackground("0000");
		StickerClassLink stickerClassLink = new StickerClassLink();
		stickerClassLink.setObjclass(Artikel.class.getSimpleName());
		stickerClassLink.setSticker(sticker);
		sticker.getStickerClassLinks().add(stickerClassLink);
		
		StickerObjectLink stickerObjectLink = new StickerObjectLink();
		stickerObjectLink.setObj(Artikel.class.getSimpleName());
		stickerObjectLink.setSticker(sticker);
		sticker.getStickerObjectLinks().add(stickerObjectLink);
		sticker = StickerService.save(sticker);
		
		assertEquals("Sticker 11", sticker.getName());
		assertEquals("0000", sticker.getBackground());
		assertEquals(1, sticker.getStickerClassLinks().size());
		assertEquals(1, sticker.getStickerObjectLinks().size());
		for (StickerClassLink stLink : sticker.getStickerClassLinks()) {
			assertEquals(Artikel.class.getSimpleName(), stLink.getObjclass());
			assertEquals(stLink.getSticker().getId(), sticker.getId());
		}
		for (StickerObjectLink stLink : sticker.getStickerObjectLinks()) {
			assertEquals(Artikel.class.getSimpleName(), stLink.getObj());
			assertEquals(stLink.getSticker().getId(), sticker.getId());
		}
		
		// update sticker class links
		assertEquals(1, sticker.getStickerClassLinks().size());
		sticker.getStickerClassLinks().remove(0);
		sticker = StickerService.save(sticker);
		assertEquals(0, sticker.getStickerClassLinks().size());
		assertEquals(1, sticker.getStickerObjectLinks().size());
	}
	
	@Test
	public void testCreateAndDeleteSticker(){
		// creates a sticker
		Sticker sticker = new Sticker();
		sticker.setBackground("0");
		sticker.setForeground("0");
		sticker.setName("Sticker 2");
		
		StickerClassLink stickerClassLink = new StickerClassLink();
		stickerClassLink.setObjclass(ArtikelstammItem.class.getSimpleName());
		stickerClassLink.setSticker(sticker);
		List<StickerClassLink> stickerClassLinks = new ArrayList<>();
		stickerClassLinks.add(stickerClassLink);
		sticker.setStickerClassLinks(stickerClassLinks);
		
		StickerObjectLink stickerObjectLink = new StickerObjectLink();
		stickerObjectLink.setObj(Artikel.class.getSimpleName());
		stickerObjectLink.setSticker(sticker);
		
		List<StickerObjectLink> stickerObjectLinks = new ArrayList<>();
		stickerObjectLinks.add(stickerObjectLink);
		sticker.setStickerObjectLinks(stickerObjectLinks);
		
		sticker = StickerService.save(sticker);
		
		assertEquals(1, StickerService.findAll(Sticker.class, false).size());
		assertEquals("Sticker 2", sticker.getName());
		assertEquals("0", sticker.getBackground());
		assertEquals(1, sticker.getStickerClassLinks().size());
		assertEquals(1, sticker.getStickerObjectLinks().size());
		for (StickerClassLink stLink : sticker.getStickerClassLinks()) {
			assertEquals(ArtikelstammItem.class.getSimpleName(), stLink.getObjclass());
			assertEquals(stLink.getSticker().getId(), sticker.getId());
		}
		for (StickerObjectLink stLink : sticker.getStickerObjectLinks()) {
			assertEquals(Artikel.class.getSimpleName(), stLink.getObj());
			assertEquals(stLink.getSticker().getId(), sticker.getId());
		}

		// removes a sticker
		StickerService.delete(StickerService.load(sticker.getId()).get());
		assertEquals(0, StickerService.findAll(Sticker.class, false).size());
		assertEquals(1, StickerService.findAll(Sticker.class, true).size());
	}
}
