package info.elexis.server.fhir.rest.core.resources.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;

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
	@Ignore(value = "validate manually, too many possible combinations")
	public void nameCoOrAddressCo() {
		StringAndListParam theFtFilter = new StringAndListParam();
		theFtFilter.addAnd(new StringParam("name co \"Muster\" or address co \"Muster\""));

		IQuery<IPatient> query = AllTests.getModelService().getQuery(IPatient.class);
		new IContactSearchFilterQueryAdapter().adapt(query, theFtFilter);
		String queryString = query.toString();
		String subString = queryString.substring(queryString.indexOf("WHERE"));

		assertThat(subString, anyOf(equalTo(
				"WHERE ((((DELETED <> ?) AND (istPerson = ?)) AND (istPatient = ?)) AND ((LOWER(bezeichnung1) LIKE ? OR LOWER(bezeichnung2) LIKE ?) OR (LOWER(strasse) LIKE ? OR LOWER(ort) LIKE ? OR LOWER(plz) LIKE ?)))"),
				equalTo("WHERE ((((DELETED <> ?) AND (istPerson = ?)) AND (istPatient = ?)) AND ((LOWER(bezeichnung1) LIKE ? OR LOWER(bezeichnung2) LIKE ?) OR ((LOWER(strasse) LIKE ? OR LOWER(ort) LIKE ?) OR LOWER(plz) LIKE ?)))")));
	}

	@Test
	@Ignore(value = "validate manually, too many possible combinations")
	public void nameCoAndAddressCo() {
		StringAndListParam theFtFilter = new StringAndListParam();
		theFtFilter.addAnd(new StringParam("name co \"Muster\" and address co \"Muster\""));

		IQuery<IPatient> query = AllTests.getModelService().getQuery(IPatient.class);
		new IContactSearchFilterQueryAdapter().adapt(query, theFtFilter);
		String queryString = query.toString();
		String subString = queryString.substring(queryString.indexOf("WHERE"));
		assertThat(subString, anyOf(equalTo(
				"WHERE ((((DELETED <> ?) AND (istPerson = ?)) AND (istPatient = ?)) AND ((LOWER(bezeichnung1) LIKE ? OR LOWER(bezeichnung2) LIKE ?) AND (LOWER(strasse) LIKE ? OR LOWER(plz) LIKE ? OR LOWER(ort) LIKE ?)))")));
	}

	@Test
	public void nameCo() {
		StringAndListParam theFtFilter = new StringAndListParam();
		theFtFilter.addAnd(new StringParam("name co \"Muster\""));

		IQuery<IPatient> query = AllTests.getModelService().getQuery(IPatient.class);
		new IContactSearchFilterQueryAdapter().adapt(query, theFtFilter);
		String queryString = query.toString();
		String subString = queryString.substring(queryString.indexOf("WHERE"));
		assertThat(subString, anyOf(equalTo(
				"WHERE ((((DELETED <> ?) AND (istPerson = ?)) AND (istPatient = ?)) AND (LOWER(bezeichnung1) LIKE ? OR LOWER(bezeichnung2) LIKE ?))"),
				equalTo("WHERE ((((DELETED <> ?) AND (istPerson = ?)) AND (istPatient = ?)) AND (LOWER(bezeichnung2) LIKE ? OR LOWER(bezeichnung1) LIKE ?))")));
	}

	@Test
	@Ignore(value = "FIX ME, not sure if this is correct?!")
	public void identifierOrAdressOrBirthdateYear() {
		StringAndListParam theFtFilter = new StringAndListParam();
		StringParam stringParam = new StringParam(
				"identifier eq \"www.elexis.info/patnr|1113\" or address co \"1113\" or birthdate eq \"1113\"");
		theFtFilter.addAnd(stringParam);

		IQuery<IPatient> query = AllTests.getModelService().getQuery(IPatient.class);
		new IContactSearchFilterQueryAdapter().adapt(query, theFtFilter);
		String queryString = query.toString();
		String subString = queryString.substring(queryString.indexOf("WHERE"));

		assertThat(subString, anyOf(equalTo(
				"WHERE ((((DELETED <> ?) AND (istPerson = ?)) AND (istPatient = ?)) AND ((patientNr = ?) OR ((LOWER(ort) LIKE ? OR LOWER(strasse) LIKE ?) OR (geburtsdatum = ?))))")));

	}

}
