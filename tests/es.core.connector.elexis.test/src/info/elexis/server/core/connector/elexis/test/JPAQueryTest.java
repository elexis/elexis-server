package info.elexis.server.core.connector.elexis.test;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription_;
import info.elexis.server.core.connector.elexis.services.JPACountQuery;
import info.elexis.server.core.connector.elexis.services.JPAQuery;

public class JPAQueryTest {

	@Test
	public void testBasicJPAQueryWithSingleCondition() {
		JPAQuery<Prescription> query = new JPAQuery<Prescription>(Prescription.class);
		query.add(Prescription_.artikel, JPAQuery.QUERY.LIKE, "ch.artikelstamm.elexis.common.ArtikelstammItem%");
		List<Prescription> resultPrescription = query.execute();
		assertNotNull(resultPrescription);
	}
	
	@Test
	public void testBasicJPAQueryWithMultipleConditions() {
		JPAQuery<ArtikelstammItem> qbe = new JPAQuery<ArtikelstammItem>(ArtikelstammItem.class);
		qbe.add(ArtikelstammItem_.bb, JPAQuery.QUERY.EQUALS, "0");
		qbe.add(ArtikelstammItem_.type, JPAQuery.QUERY.EQUALS, "P");
		qbe.add(ArtikelstammItem_.cummVersion, JPAQuery.QUERY.LESS_OR_EQUAL, "35");

		List<ArtikelstammItem> qre = qbe.execute();
		assertNotNull(qre);
	}
	
	@Test
	public void testJPACountQueryWithMultipleConditions() {
		JPACountQuery<ArtikelstammItem> qbec = new JPACountQuery<ArtikelstammItem>(ArtikelstammItem.class);
		qbec.add(ArtikelstammItem_.bb, JPACountQuery.QUERY.EQUALS, "0");
		qbec.add(ArtikelstammItem_.type, JPACountQuery.QUERY.EQUALS, "P");
		qbec.add(ArtikelstammItem_.cummVersion, JPACountQuery.QUERY.LESS_OR_EQUAL, "35");

		long result = qbec.count();
		assertTrue(result > 5);
	}

}
