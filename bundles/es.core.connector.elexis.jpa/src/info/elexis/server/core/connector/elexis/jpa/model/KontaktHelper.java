package info.elexis.server.core.connector.elexis.jpa.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import info.elexis.server.core.connector.elexis.jpa.manager.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Brief;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.types.ContactType;

public class KontaktHelper {

	private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

	public static String getLabel(Kontakt k, boolean includeDateOfBirth) {
		ContactType contactType = k.getContactType();
		switch (contactType) {
		case PERSON:
			boolean istPatient = k.isIstPatient();
			if (istPatient) {
				Date geburtsdatum = k.getGeburtsdatum();
				if (!includeDateOfBirth) {
					return k.getBezeichnung2() + "," + k.getBezeichnung1() + " [" + k.getPatientNr() + "]";
				}
				String gbd = (geburtsdatum != null) ? "(" + sdf.format(k.getGeburtsdatum()) + ")" : "";
				return k.getBezeichnung2() + "," + k.getBezeichnung1() + " " + gbd + " [" + k.getPatientNr() + "]";
			}

			return k.getBezeichnung2() + " " + k.getBezeichnung1();
		case ORGANIZATION:
			return k.getBezeichnung1() + " " + k.getBezeichnung2();
		default:
			return k.getId() + " " + k.getContactType().name();
		}
	}

	private static Pattern pattern = Pattern.compile(".*\\[(.*?)\\].*");

	public static Kontakt parseURILabel(String uriLabel) {
		Matcher matcher = pattern.matcher(uriLabel);
		if (matcher.find()) {
			String patientId = matcher.group(1);
			Query nq = ElexisEntityManager.em().createNamedQuery("FindContactByPatientId");
			nq.setParameter(1, patientId);
			return (Kontakt) nq.getSingleResult();
		}

		return null;

	}

	public static List<Brief> getBriefeForKontakt(Kontakt contact) {
		CriteriaBuilder cb = ElexisEntityManager.em().getCriteriaBuilder();

		// Query for a List of objects.
		CriteriaQuery cq = cb.createQuery();
		Root e = cq.from(Brief.class);
		cq.where(cb.equal(e.get("patient"), contact));
		Query query = ElexisEntityManager.em().createQuery(cq);
		List<Brief> result = query.getResultList();
		return result;
	}

}
