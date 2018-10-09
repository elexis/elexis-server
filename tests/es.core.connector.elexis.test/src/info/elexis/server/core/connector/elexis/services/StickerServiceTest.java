//package info.elexis.server.core.connector.elexis.services;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//
//import java.util.List;
//import java.util.Optional;
//
//import org.junit.Test;
//
//import info.elexis.server.core.connector.elexis.jpa.ElexisTypeMap;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Artikel;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Sticker;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.StickerClassLink;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.StickerObjectLink;
//import info.elexis.server.core.connector.elexis.jpa.test.TestEntities;
//
//public class StickerServiceTest extends AbstractServiceTest {
//
//	@Test
//	public void testCreateAndUpdateSticker() {
//		// creates a sticker
//		Sticker sticker = new Sticker();
//		sticker.setBackground("123456");
//		sticker.setForeground("123456");
//		sticker.setName("Sticker 1");
//
//		sticker = StickerService.save(sticker);
//
//		assertEquals(1, StickerService.findAll(Sticker.class, false).size());
//		assertEquals("Sticker 1", sticker.getName());
//		assertEquals(0, sticker.getStickerClassLinks().size());
//
//		// updates a sticker
//		sticker.setName("Sticker 11");
//		sticker.setBackground("0000");
//		StickerClassLink stickerClassLink = new StickerClassLink();
//		stickerClassLink.setObjclass(Artikel.class.getSimpleName());
//		stickerClassLink.setSticker(sticker);
//		sticker.getStickerClassLinks().add(stickerClassLink);
//
//		StickerObjectLink stickerObjectLink = new StickerObjectLink();
//		stickerObjectLink.setObj(Artikel.class.getSimpleName());
//		stickerObjectLink.setSticker(sticker);
//		sticker.getStickerObjectLinks().add(stickerObjectLink);
//		sticker = StickerService.save(sticker);
//
//		assertEquals("Sticker 11", sticker.getName());
//		assertEquals("0000", sticker.getBackground());
//		assertEquals(1, sticker.getStickerClassLinks().size());
//		assertEquals(1, sticker.getStickerObjectLinks().size());
//		for (StickerClassLink stLink : sticker.getStickerClassLinks()) {
//			assertEquals(Artikel.class.getSimpleName(), stLink.getObjclass());
//			assertEquals(stLink.getSticker().getId(), sticker.getId());
//		}
//		for (StickerObjectLink stLink : sticker.getStickerObjectLinks()) {
//			assertEquals(Artikel.class.getSimpleName(), stLink.getObj());
//			assertEquals(stLink.getSticker().getId(), sticker.getId());
//		}
//
//		// update sticker class links
//		assertEquals(1, sticker.getStickerClassLinks().size());
//		StickerClassLink next = sticker.getStickerClassLinks().iterator().next();
//		sticker.getStickerClassLinks().remove(next);
//		sticker = StickerService.save(sticker);
//		assertEquals(0, sticker.getStickerClassLinks().size());
//		assertEquals(1, sticker.getStickerObjectLinks().size());
//		
//		StickerService.remove(sticker);
//	}
//
//	@Test
//	public void testCreateAndDeleteSticker() {
//		// creates a sticker
//		Sticker sticker = new Sticker();
//		sticker.setBackground("0");
//		sticker.setForeground("0");
//		sticker.setName("Sticker 2");
//
//		StickerClassLink stickerClassLink = new StickerClassLink();
//		stickerClassLink.setObjclass(ArtikelstammItem.class.getSimpleName());
//		stickerClassLink.setSticker(sticker);
//		sticker.getStickerClassLinks().add(stickerClassLink);
//
//		StickerObjectLink stickerObjectLink = new StickerObjectLink();
//		stickerObjectLink.setObj(Artikel.class.getSimpleName());
//		stickerObjectLink.setSticker(sticker);
//
//		sticker.getStickerObjectLinks().add(stickerObjectLink);
//
//		sticker = StickerService.save(sticker);
//
//		assertEquals(1, StickerService.findAll(Sticker.class, false).size());
//		assertEquals("Sticker 2", sticker.getName());
//		assertEquals("0", sticker.getBackground());
//		assertEquals(1, sticker.getStickerClassLinks().size());
//		assertEquals(1, sticker.getStickerObjectLinks().size());
//		for (StickerClassLink stLink : sticker.getStickerClassLinks()) {
//			assertEquals(ArtikelstammItem.class.getSimpleName(), stLink.getObjclass());
//			assertEquals(stLink.getSticker().getId(), sticker.getId());
//		}
//		for (StickerObjectLink stLink : sticker.getStickerObjectLinks()) {
//			assertEquals(Artikel.class.getSimpleName(), stLink.getObj());
//			assertEquals(stLink.getSticker().getId(), sticker.getId());
//		}
//
//		// removes a sticker
//		StickerService.delete(StickerService.load(sticker.getId()).get());
//		assertEquals(0, StickerService.findAll(Sticker.class, false).size());
//		assertEquals(1, StickerService.findAll(Sticker.class, true).size());
//	}
//
//	@Test
//	public void testFindStickersApplicableToPatients() {
//		Sticker sticker = new Sticker();
//		sticker.setBackground("0");
//		sticker.setForeground("0");
//		sticker.setName("Sticker 2");
//
//		StickerClassLink stickerClassLink = new StickerClassLink();
//		stickerClassLink.setObjclass(ElexisTypeMap.TYPE_PATIENT);
//		stickerClassLink.setSticker(sticker);
//		sticker.getStickerClassLinks().add(stickerClassLink);
//
//		sticker = StickerService.save(sticker);
//
//		List<Sticker> patientApplicableStickers = StickerService
//				.findStickersApplicableToClass(ElexisTypeMap.TYPE_PATIENT);
//		assertEquals(1, patientApplicableStickers.size());
//
//		boolean stickerApplicableToClass = StickerService.isStickerApplicableToClass(sticker,
//				ElexisTypeMap.TYPE_PATIENT);
//		assertTrue(stickerApplicableToClass);
//		stickerApplicableToClass = StickerService.isStickerApplicableToClass(sticker, ElexisTypeMap.TYPE_EIGENARTIKEL);
//		assertFalse(stickerApplicableToClass);
//
//		StickerService.remove(sticker);
//	}
//
//	@Test
//	public void testApplyRemoveStickerToPatient() {
//		
//		Sticker sticker = new StickerService.StickerBuilder("Sticker 2", "123456", "123456", ElexisTypeMap.TYPE_PATIENT)
//				.buildAndSave();
//
//		assertEquals(1, StickerService.findAll(Sticker.class, false).size());
//		assertEquals("Sticker 2", sticker.getName());
//		assertEquals(1, sticker.getStickerClassLinks().size());
//		assertEquals(ElexisTypeMap.TYPE_PATIENT, sticker.getStickerClassLinks().iterator().next().getObjclass());
//		Optional<Kontakt> findById = KontaktService.load(TestEntities.PATIENT_MALE_ID);
//
//		List<Sticker> findStickersOnObject = StickerService.findStickersOnObject(findById.get());
//		assertEquals(0, findStickersOnObject.size());
//		
//		boolean stickerApplied = StickerService.applyStickerToObject(sticker, findById.get());
//		assertTrue(stickerApplied);
//		// deliberately try twice to see that the sum still is 1
//		stickerApplied = StickerService.applyStickerToObject(sticker, findById.get());
//		assertTrue(stickerApplied);
//		
//		findStickersOnObject = StickerService.findStickersOnObject(findById.get());
//		assertEquals(1, findStickersOnObject.size());
//		
//		StickerService.removeAllStickersFromObject(findById.get());
//
//		findStickersOnObject = StickerService.findStickersOnObject(findById.get());
//		assertEquals(0, findStickersOnObject.size());
//		
//		StickerService.remove(sticker);
//	}
//}
