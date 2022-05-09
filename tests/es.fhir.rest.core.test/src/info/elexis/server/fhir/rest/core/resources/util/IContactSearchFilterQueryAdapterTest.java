package info.elexis.server.fhir.rest.core.resources.util;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IQuery;
import es.fhir.rest.core.resources.util.IContactSearchFilterQueryAdapter;
import info.elexis.server.fhir.rest.core.test.AllTests;

public class IContactSearchFilterQueryAdapterTest {

	@Test
	public void nameCoOrAddressCo() {
		StringAndListParam theFtFilter = new StringAndListParam();
		theFtFilter.addAnd(new StringParam("name co \"Muster\" or address co \"Muster\""));

		IQuery<IPatient> query = AllTests.getModelService().getQuery(IPatient.class);
		new IContactSearchFilterQueryAdapter().adapt(query, theFtFilter);
		String queryString = query.toString();
		String subString = queryString.substring(queryString.indexOf("WHERE"));
		assertEquals(
				"WHERE ((((DELETED <> ?) AND (istPerson = ?)) AND (istPatient = ?)) AND ((LOWER(bezeichnung1) LIKE ? OR LOWER(bezeichnung2) LIKE ?) OR (LOWER(strasse) LIKE ? OR LOWER(ort) LIKE ?)))",
				subString);
	}

	@Test
	@Ignore("does not yet work")
	public void nameCoAndAddressCo() {
		StringAndListParam theFtFilter = new StringAndListParam();
		theFtFilter.addAnd(new StringParam("name co \"Muster\" and address co \"Muster\""));

		IQuery<IPatient> query = AllTests.getModelService().getQuery(IPatient.class);
		new IContactSearchFilterQueryAdapter().adapt(query, theFtFilter);
		String queryString = query.toString();
		String subString = queryString.substring(queryString.indexOf("WHERE"));
		assertEquals(
				"WHERE ((((DELETED <> ?) AND (istPerson = ?)) AND (istPatient = ?)) AND ((LOWER(bezeichnung1) LIKE ? OR LOWER(bezeichnung2) LIKE ?) AND (LOWER(strasse) LIKE ? OR LOWER(ort) LIKE ?)))",
				subString);
	}

	@Test
	public void nameCo() {
		StringAndListParam theFtFilter = new StringAndListParam();
		theFtFilter.addAnd(new StringParam("name co \"Muster\""));

		IQuery<IPatient> query = AllTests.getModelService().getQuery(IPatient.class);
		new IContactSearchFilterQueryAdapter().adapt(query, theFtFilter);
		String queryString = query.toString();
		String subString = queryString.substring(queryString.indexOf("WHERE"));
		assertEquals(
				"WHERE ((((DELETED <> ?) AND (istPerson = ?)) AND (istPatient = ?)) AND (LOWER(bezeichnung1) LIKE ? OR LOWER(bezeichnung2) LIKE ?))",
				subString);
	}

}
