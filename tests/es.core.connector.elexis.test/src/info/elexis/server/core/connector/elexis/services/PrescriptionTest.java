package info.elexis.server.core.connector.elexis.services;

public class PrescriptionTest {

//	@Test
//	public void testGetArtikelFromPrescription() {
//		JPAQuery<Prescription> query = new JPAQuery<Prescription>(Prescription.class);
//		query.add(Prescription_.artikel, JPAQuery.QUERY.LIKE, "ch.artikelstamm.elexis.common.ArtikelstammItem%");
//		List<Prescription> resultPrescription = query.execute();
//		assertTrue(resultPrescription.size() > 0);
//
//		AbstractDBObject artikel = resultPrescription.get(0).getArtikel();
//		assertNotNull(artikel);
//		assertTrue(artikel instanceof ArtikelstammItem);
//	}
//
//	@Test
//	public void testSetArtikelOnPrescriptionViaStoreToString() {
//		JPAQuery<Prescription> query = new JPAQuery<Prescription>(Prescription.class);
//		query.add(Prescription_.artikel, JPAQuery.QUERY.LIKE, "ch.artikelstamm.elexis.common.ArtikelstammItem%");
//		List<Prescription> resultPrescription = query.execute();
//		assertTrue(resultPrescription.size() > 0);
//
//		Prescription prescription = resultPrescription.get(0);
//		JPAQuery<ArtikelstammItem> qb = new JPAQuery<ArtikelstammItem>(ArtikelstammItem.class);
//		qb.add(ArtikelstammItem_.dscr, JPAQuery.QUERY.LIKE, "V%");
//		List<ArtikelstammItem> execute = qb.execute();
//
//		// select a random article, if we do this deterministic, no changes
//		// happen
//		assertTrue(execute.size() > 1);
//		int rand = (int) (Math.random() * 10);
//		prescription.setArtikel(execute.get(rand % execute.size()));
//	}

}
