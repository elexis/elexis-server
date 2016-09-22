package info.elexis.server.core.connector.elexis.billable;

import static org.junit.Assert.*;

import org.junit.Test;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.services.ArtikelstammItemService;

public class VerrechenbarTest {

	/**
	 * https://redmine.medelexis.ch/issues/4783
	 */
	@Test
	public void testReturnCorrectCodeSystemCodeForArtikelstammItem() {
		ArtikelstammItem steristrip = ArtikelstammItemService.INSTANCE.findById("0200069235501108692350008").get();
		VerrechenbarArtikelstammItem vai = new VerrechenbarArtikelstammItem(steristrip);
		assertEquals("406", vai.getCodeSystemCode());

		ArtikelstammItem procedurepack = ArtikelstammItemService.INSTANCE.findById("0733243060721831033730008").get();
		VerrechenbarArtikelstammItem vai2 = new VerrechenbarArtikelstammItem(procedurepack);
		assertEquals("406", vai2.getCodeSystemCode());
		
		ArtikelstammItem dafalgan = ArtikelstammItemService.INSTANCE.findById("0768047504023220684270008").get();
		VerrechenbarArtikelstammItem vai3 = new VerrechenbarArtikelstammItem(dafalgan);
		assertEquals("402", vai3.getCodeSystemCode());
	}
}
