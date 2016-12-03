package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.List;

import org.eclipse.persistence.queries.ScrollableCursor;
import org.junit.Test;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObject_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription_;
import info.elexis.server.core.connector.elexis.services.JPAQuery.ORDER;

public class JPAQueryTest {

	@Test
	public void testJPAQueryWithoutCondition() {
		List<Kontakt> result = new JPAQuery<Kontakt>(Kontakt.class).execute();
		assertNotNull(result);
		assertTrue(result.size() > 0);
		List<Kontakt> resultIncDeleted = new JPAQuery<Kontakt>(Kontakt.class, true).execute();
		assertNotNull(resultIncDeleted);
		assertTrue(resultIncDeleted.size() == 5);
	}

	@Test
	public void testBasicJPAQueryWithSingleCondition() {
		JPAQuery<Prescription> query = new JPAQuery<Prescription>(Prescription.class);
		query.add(Prescription_.artikel, JPAQuery.QUERY.LIKE, "ch.artikelstamm.elexis.common.ArtikelstammItem%");
		List<Prescription> resultPrescription = query.execute();
		assertNotNull(resultPrescription);
		assertTrue(resultPrescription.size() == 0);
	}

	@Test
	public void testBasicJPAQueryWithMultipleConditions() {
		JPAQuery<ArtikelstammItem> qbe = new JPAQuery<ArtikelstammItem>(ArtikelstammItem.class);
		qbe.add(ArtikelstammItem_.bb, JPAQuery.QUERY.EQUALS, "0");
		qbe.add(ArtikelstammItem_.type, JPAQuery.QUERY.EQUALS, "P");
		qbe.add(ArtikelstammItem_.cummVersion, JPAQuery.QUERY.LESS_OR_EQUAL, "8");

		List<ArtikelstammItem> qre = qbe.execute();
		assertNotNull(qre);
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
		assertTrue("Size is " + id, id == 18);
	}

	@Test
	public void testJPACountQueryWithMultipleConditions() {
		JPACountQuery<ArtikelstammItem> qbec = new JPACountQuery<ArtikelstammItem>(ArtikelstammItem.class);
		qbec.add(ArtikelstammItem_.bb, JPACountQuery.QUERY.EQUALS, "0");
		qbec.add(ArtikelstammItem_.type, JPACountQuery.QUERY.EQUALS, "P");
		qbec.add(ArtikelstammItem_.cummVersion, JPACountQuery.QUERY.LESS_OR_EQUAL, "8");

		long result = qbec.count();
		assertTrue(result == 17);

		JPACountQuery<ArtikelstammItem> qbecD = new JPACountQuery<ArtikelstammItem>(ArtikelstammItem.class, true);
		qbecD.add(ArtikelstammItem_.bb, JPACountQuery.QUERY.EQUALS, "0");
		qbecD.add(ArtikelstammItem_.type, JPACountQuery.QUERY.EQUALS, "P");
		qbecD.add(ArtikelstammItem_.cummVersion, JPACountQuery.QUERY.LESS_OR_EQUAL, "8");

		long resultD = qbecD.count();
		assertTrue(resultD == 18);
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

}
