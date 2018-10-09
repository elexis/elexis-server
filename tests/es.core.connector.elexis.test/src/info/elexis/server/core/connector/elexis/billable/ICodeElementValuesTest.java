package info.elexis.server.core.connector.elexis.billable;

public class ICodeElementValuesTest {

//	@Test
//	public void testICodeElementTarmed() {
//		assertICodeElement(TarmedLeistungService.load("00.0060-20120601").get(), "00.0060", "999",
//				TarmedLeistung.CODESYSTEM_NAME, "Besuch, erste 5 Min. (Grundbesuch)");
//		assertICodeElement(TarmedLeistungService.findFromCode("39.06.03").get(), "39.06.03", "999",
//				TarmedLeistung.CODESYSTEM_NAME, "Angiografie: Pfortader");
//	}
//
//	@Test
//	public void testICodeElementArtikel() {
//		// Eigenartikel
//		Artikel ea1 = new ArtikelService.Builder("Name", "InternalName", Artikel.TYP_EIGENARTIKEL).build();
//		ea1.setSubId("2931229");
//		ArtikelService.save(ea1);
//		assertICodeElement(ea1, "2931229", "999", "Eigenartikel", "InternalName");
//		// TODO other?
//	}
//
//	@Test
//	public void testICodeElementArtikelstamm() {
//		assertICodeElement(ArtikelstammItemService.load("0768053160026405519870008").get(), "0551987", "402",
//				ArtikelstammItem.CODESYSTEM_NAME, "SINUPRET Tropfen 100 ml");
//	}
//
//	@Test
//	public void testICodeElementLaborTarif2009() {
//		assertICodeElement(Labor2009TarifService.load("Z840838a7800980f02839").get(), "3358.00", "317",
//				Labor2009Tarif.CODESYSTEM_NAME,
//				"Spezielle Mikroskopie (Acridineorange, Ziehl-Neelsen, Auramin-Rhodamin,");
//	}
//
//	@Test
//	@Ignore
//	public void testICodeElementPhysioLeistung() {
//		assertICodeElement(PhysioLeistungService.load("Z840838a7800980f02839").get(), "3358.00", "317",
//				PhysioLeistung.CODESYSTEM_NAME,
//				"Spezielle Mikroskopie (Acridineorange, Ziehl-Neelsen, Auramin-Rhodamin,");
//	}
//
//	@Test
//	public void testICodeElementLeistungsblock() {
//		assertICodeElement(LeistungsblockService.load("V535469d51e5785a20985").get(), "b7", "999",
//				Leistungsblock.CODESYSTEM_NAME, "b7");
//	}
//
//	@Test
//	@Ignore
//	public void testICodeElementLoinc() {
//	}
//
//	@Test
//	@Ignore
//	public void testICodeElementFreeTextDiagnose() {
//	}
//
//	@Test
//	@Ignore
//	public void testICodeElementEigendiagnose() {
//	}
//
//	@Test
//	@Ignore
//	public void testICodeElementICD10() {
//	}
//
//	@Test
//	@Ignore
//	public void testICodeElementIcpc() {
//	}
//
//	@Test
//	public void testICodeElementTessinerCode() {
//		// comparing null on text due to i18n differences
//		assertICodeElement(TessinerCode.load("A4"), "A4", "999", TessinerCode.CODESYSTEM_NAME, null);
//	}
//
//	private void assertICodeElement(ICodeElement iCodeElement, String code, String systemCode, String systemName,
//			String text) {
//		assertEquals(code, iCodeElement.getCode());
//		assertEquals(systemCode, iCodeElement.getCodeSystemCode());
//		assertEquals(systemName, iCodeElement.getCodeSystemName());
//		if (text != null) {
//			assertEquals(text, iCodeElement.getText());
//		}
//	}

}
