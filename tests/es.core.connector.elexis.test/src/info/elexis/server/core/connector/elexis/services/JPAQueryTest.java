package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

import org.eclipse.persistence.queries.ScrollableCursor;
import org.junit.Test;

import ch.elexis.core.types.Gender;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObject_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung_;
import info.elexis.server.core.connector.elexis.services.JPAQuery.ORDER;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;

public class JPAQueryTest {

	@Test
	public void testJPAQueryWithoutCondition() {
		List<Kontakt> result = new JPAQuery<Kontakt>(Kontakt.class).execute();
		assertNotNull(result);
		assertTrue(result.size() > 0);
		List<Kontakt> resultIncDeleted = new JPAQuery<Kontakt>(Kontakt.class, true).execute();
		assertNotNull(resultIncDeleted);
		assertEquals(7, resultIncDeleted.size());

		long count = new JPAQuery<Kontakt>(Kontakt.class, true).count();
		assertEquals(resultIncDeleted.size(), count);
	}

	@Test
	public void testBasicJPAQueryWithSingleCondition() {
		JPAQuery<Prescription> query = new JPAQuery<Prescription>(Prescription.class);
		query.add(Prescription_.artikel, JPAQuery.QUERY.LIKE, "ch.artikelstamm.elexis.common.ArtikelstammItem%");
		List<Prescription> resultPrescription = query.execute();
		assertNotNull(resultPrescription);
		assertTrue(resultPrescription.size() == 0);

		assertEquals(resultPrescription.size(), query.count());
	}

	@Test
	public void testBasicJPAQueryWithMultipleConditions() {
		JPAQuery<ArtikelstammItem> qbe = new JPAQuery<ArtikelstammItem>(ArtikelstammItem.class);
		qbe.add(ArtikelstammItem_.bb, JPAQuery.QUERY.EQUALS, "0");
		qbe.add(ArtikelstammItem_.type, JPAQuery.QUERY.EQUALS, "P");
		qbe.add(ArtikelstammItem_.cummVersion, JPAQuery.QUERY.LESS_OR_EQUAL, "8");

		List<ArtikelstammItem> qre = qbe.execute();
		assertNotNull(qre);

		assertEquals(qre.size(), qbe.count());
	}
	
	@Test
	public void testJPAQueryFilterGreaterOrEquals() {
		JPAQuery<Kontakt> qbe = new JPAQuery<Kontakt>(Kontakt.class);
		qbe.add(AbstractDBObject_.lastupdate, QUERY.GREATER_OR_EQUAL, 1470809122982l);
		List<Kontakt> execute = qbe.execute();
		assertEquals(6, execute.size());
		assertEquals(6, qbe.count());
		
		JPAQuery<Kontakt> qbe2 = new JPAQuery<Kontakt>(Kontakt.class);
		qbe2.add(AbstractDBObject_.lastupdate, QUERY.GREATER, 1470809122982l);
		List<Kontakt> execute2 = qbe2.execute();
		assertEquals(5, execute2.size());
		assertEquals(5, qbe2.count());
	}

	@Test
	public void testBasicJPAQueryWithMultipleConditionsAsCursor() {
		JPAQuery<ArtikelstammItem> qbe = new JPAQuery<ArtikelstammItem>(ArtikelstammItem.class);
		qbe.add(ArtikelstammItem_.bb, JPAQuery.QUERY.EQUALS, "0");
		qbe.add(ArtikelstammItem_.type, JPAQuery.QUERY.EQUALS, "P");
		qbe.add(ArtikelstammItem_.cummVersion, JPAQuery.QUERY.LESS_OR_EQUAL, "8");

		ScrollableCursor cursor = qbe.executeAsStream();
		int i = 0;
		while (cursor.hasNext()) {
			i++;
			ArtikelstammItem ai = (ArtikelstammItem) cursor.next();
			assertNotNull(ai);
			assertEquals("0", ai.getBb());
			assertEquals("P", ai.getType());
			cursor.clear();
		}
		cursor.close();
		assertTrue("Size is " + i, i == 17);

		JPAQuery<ArtikelstammItem> qbeD = new JPAQuery<ArtikelstammItem>(ArtikelstammItem.class, true);
		qbeD.add(ArtikelstammItem_.bb, JPAQuery.QUERY.EQUALS, "0");
		qbeD.add(ArtikelstammItem_.type, JPAQuery.QUERY.EQUALS, "P");
		qbeD.add(ArtikelstammItem_.cummVersion, JPAQuery.QUERY.LESS_OR_EQUAL, "8");

		ScrollableCursor cursorD = qbeD.executeAsStream();
		int id = 0;
		while (cursorD.hasNext()) {
			id++;
			ArtikelstammItem ai = (ArtikelstammItem) cursorD.next();
			assertNotNull(ai);
			assertEquals("0", ai.getBb());
			assertEquals("P", ai.getType());
		}
		cursorD.close();
		assertEquals(id, qbeD.count());
		assertTrue("Size is " + id, id == 18);
	}

	@Test
	public void testJPAQueryOrdered() {
		JPAQuery<ArtikelstammItem> qbe = new JPAQuery<ArtikelstammItem>(ArtikelstammItem.class);
		qbe.add(ArtikelstammItem_.bb, JPAQuery.QUERY.EQUALS, "0");
		qbe.add(ArtikelstammItem_.type, JPAQuery.QUERY.EQUALS, "P");
		qbe.add(ArtikelstammItem_.cummVersion, JPAQuery.QUERY.LESS_OR_EQUAL, "8");

		ScrollableCursor cursor = qbe.executeAsStream(ArtikelstammItem_.gtin, ORDER.DESC);
		int i = 0;
		while (cursor.hasNext()) {
			i++;
			ArtikelstammItem ai = (ArtikelstammItem) cursor.next();
			assertNotNull(ai);
			assertEquals("0", ai.getBb());
			assertEquals("P", ai.getType());
			cursor.clear();
		}
		cursor.close();
		assertEquals(i, qbe.count());
		assertTrue("Size is " + i, i == 17);
	}

	@Test
	public void testJPAQueryOrderedLimited() {
		JPAQuery<ArtikelstammItem> qbe = new JPAQuery<ArtikelstammItem>(ArtikelstammItem.class);
		qbe.add(ArtikelstammItem_.bb, JPAQuery.QUERY.EQUALS, "0");
		qbe.add(ArtikelstammItem_.type, JPAQuery.QUERY.EQUALS, "P");
		qbe.add(ArtikelstammItem_.cummVersion, JPAQuery.QUERY.LESS_OR_EQUAL, "8");

		ScrollableCursor cursor = qbe.executeAsStream(AbstractDBObject_.lastupdate, ORDER.DESC, 5, 10);
		int i = 0;
		BigInteger lastUpdate = BigInteger.valueOf(Long.MAX_VALUE);
		while (cursor.hasNext()) {
			i++;
			ArtikelstammItem ai = (ArtikelstammItem) cursor.next();
			BigInteger lastupdate2 = ai.getLastupdate();
			assertTrue(lastupdate2.longValue() < lastUpdate.longValue());
			lastUpdate = lastupdate2;
			assertNotNull(ai);
			assertEquals("0", ai.getBb());
			assertEquals("P", ai.getType());
			cursor.clear();
		}
		cursor.close();
		assertTrue("Size is " + i, i == 10);
	}

	@Test
	public void testJPAQueryNotEquals() {
		JPAQuery<ArtikelstammItem> qbe = new JPAQuery<ArtikelstammItem>(ArtikelstammItem.class);
		qbe.add(ArtikelstammItem_.type, QUERY.NOT_EQUALS, "P");
		List<ArtikelstammItem> execute = qbe.execute();
		for (ArtikelstammItem ai : execute) {
			assertFalse(ai.getType().equalsIgnoreCase("P"));
		}
		assertEquals(execute.size(), qbe.count());
		assertTrue(execute.size() > 0);
		
		JPAQuery<Kontakt> qbeC = new JPAQuery<Kontakt>(Kontakt.class);
		qbeC.add(Kontakt_.country, QUERY.NOT_EQUALS, null);
		long count = qbeC.count();
		assertEquals(2, count);
	}
	
	@Test
	public void testJPAQueryWithOr() {
		String name = "Vorname";
		JPAQuery<Kontakt> query = new JPAQuery<>(Kontakt.class);
		query.add(Kontakt_.description1, QUERY.LIKE, "%" + name + "%");
		query.or(Kontakt_.description2, QUERY.LIKE, "%" + name + "%");
		assertEquals(2, query.count());
	}

	@Test
	public void testJPAQueryWithWildcard() {
		JPAQuery<TarmedLeistung> qbe = new JPAQuery<TarmedLeistung>(TarmedLeistung.class);
		qbe.add(TarmedLeistung_.tx255, QUERY.LIKE, "Oeso%");
		List<TarmedLeistung> execute = qbe.execute();
		assertEquals(qbe.count(), execute.size());
	}

	@Test
	public void testJPAQueryWithNonNull() {
		Kontakt testPatient = KontaktService.INSTANCE.createPatient("FirstName", null, LocalDate.now(), Gender.MALE);

		JPAQuery<Kontakt> qbe = new JPAQuery<Kontakt>(Kontakt.class);
		qbe.add(Kontakt_.description1, QUERY.EQUALS, null);
		List<Kontakt> execute = qbe.execute();
		assertEquals(1, execute.size());

		KontaktService.INSTANCE.remove(testPatient);
	}
}
