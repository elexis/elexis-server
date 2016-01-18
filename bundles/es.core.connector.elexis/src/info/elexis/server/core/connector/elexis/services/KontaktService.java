package info.elexis.server.core.connector.elexis.services;

import java.text.SimpleDateFormat;
import java.util.Date;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.types.ContactType;

public class KontaktService extends AbstractService<Kontakt>{

	public static KontaktService INSTANCE = InstanceHolder.INSTANCE;
	
	private static final class InstanceHolder {
		static final KontaktService INSTANCE = new KontaktService();
	}

	private KontaktService() {
		super(Kontakt.class);
	}

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
}
