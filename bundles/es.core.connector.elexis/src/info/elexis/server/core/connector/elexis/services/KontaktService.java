package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

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
}
