package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Config;
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

	/**
	 * 
	 * @return a managed {@link Kontakt} entity
	 */
	public Kontakt createPatient() {
		Kontakt pat = create(false);
		pat.setIstPatient(true);
		pat.setIstPerson(true);
		pat.setPatientNr(Integer.toString(findAndIncrementPatientNr()));
		flush();
		return pat;
	}

	/**
	 * Finds the current patient number, checks for uniqueness, retrieves it and
	 * increments by one
	 * 
	 * @return
	 */
	private int findAndIncrementPatientNr() {
		int ret = 0;
		EntityManager em = ElexisEntityManager.createEntityManager();
		try {
			em.getTransaction().begin();
			Config patNr = em.find(Config.class, "PatientNummer");
			if (patNr == null) {
				Config patNrConfig = new Config();
				patNrConfig.setParam("PatientNummer");
				patNrConfig.setWert("1");
				em.persist(patNrConfig);
				ret = 1;
			} else {
				em.lock(patNr, LockModeType.PESSIMISTIC_WRITE);
				ret = Integer.parseInt(patNr.getWert());
				ret += 1;

				while (true) {
					@SuppressWarnings("rawtypes")
					List resultList = em.createQuery("SELECT k FROM Kontakt k WHERE k.patientNr=" + ret)
							.getResultList();
					if (resultList.size() == 0) {
						break;
					} else {
						ret += 1;
					}
				}

				patNr.setWert(Integer.toString(ret));
			}
			em.getTransaction().commit();
			return ret;
		} finally {
			em.close();
		}
	}

	/**
	 * 
	 * @param k
	 * @return the age in years, or -1 if not applicable
	 */
	public static int getAgeInYears(Kontakt k) {
		LocalDate dob = k.getGeburtsdatum();
		if (dob == null) {
			return -1;
		}

		LocalDate now = LocalDate.now();
		long years = ChronoUnit.YEARS.between(dob, now);
		return (int) years;
	}
}
