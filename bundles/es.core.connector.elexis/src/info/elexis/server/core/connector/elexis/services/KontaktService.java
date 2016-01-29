package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt_;

public class KontaktService extends AbstractService<Kontakt> {

	public static KontaktService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final KontaktService INSTANCE = new KontaktService();
	}

	private KontaktService() {
		super(Kontakt.class);
	}

	private final DateTimeFormatter sdf = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	public String getLabel(Kontakt k, boolean includeDateOfBirth) {
		if (k.isIstPerson()) {
			boolean istPatient = k.isIstPatient();
			if (istPatient) {
				LocalDate geburtsdatum = k.getGeburtsdatum();
				if (!includeDateOfBirth) {
					return k.getBezeichnung2() + "," + k.getBezeichnung1() + " [" + k.getPatientNr() + "]";
				}
				String gbd = (geburtsdatum != null) ? "(" + k.getGeburtsdatum().format(sdf) + ")" : "";
				return k.getBezeichnung2() + "," + k.getBezeichnung1() + " " + gbd + " [" + k.getPatientNr() + "]";
			}

			return k.getBezeichnung2() + " " + k.getBezeichnung1();
		} else if (k.isIstOrganisation()) {
			return k.getBezeichnung1() + " " + k.getBezeichnung2();
		} else {
			return k.getId() + " " + k.getBezeichnung1();
		}
	}

	public List<Kontakt> findAllPatients(boolean b) {
		JPAQuery<Kontakt> qre = new JPAQuery<Kontakt>(Kontakt.class);
		qre.add(Kontakt_.istPatient, JPAQuery.QUERY.EQUALS, true);
		// qre.add(Kontakt_.istPerson, JPAQuery.QUERY.EQUALS, true);
		// not defined in Elexis Patient#getConstraint
		return qre.execute();
	}
}
