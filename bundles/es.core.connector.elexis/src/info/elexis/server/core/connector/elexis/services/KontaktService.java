package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import ch.elexis.core.model.IContact;
import ch.elexis.core.types.Gender;
import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Config;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt_;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;

public class KontaktService extends AbstractService<Kontakt> {

	public static KontaktService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final KontaktService INSTANCE = new KontaktService();
	}

	private KontaktService() {
		super(Kontakt.class);
	}

	/**
	 * 
	 * @return a managed {@link Kontakt} entity
	 */
	public Kontakt createPatient(String firstName, String lastName, LocalDate dateOfBirth, Gender sex) {
		em.getTransaction().begin();
		Kontakt pat = create(false);
		pat.setPatient(true);
		pat.setPerson(true);
		pat.setCode(Integer.toString(findAndIncrementPatientNr()));
		pat.setDescription1(lastName);
		pat.setDescription2(firstName);
		pat.setDob(dateOfBirth);
		pat.setGender(sex);
		em.getTransaction().commit();
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
					List resultList = em.createQuery("SELECT k FROM Kontakt k WHERE k.code=" + ret).getResultList();
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

	public static List<Fall> getFaelle(Kontakt k) {
		JPAQuery<Fall> query = new JPAQuery<Fall>(Fall.class);
		query.add(Fall_.patientKontakt, QUERY.EQUALS, k);
		return query.execute();
	}

	/**
	 * 
	 * @param k
	 * @return the age in years, or -1 if not applicable
	 */
	public static int getAgeInYears(Kontakt k) {
		LocalDate dob = k.getDob();
		if (dob == null) {
			return -1;
		}

		LocalDate now = LocalDate.now();
		long years = ChronoUnit.YEARS.between(dob, now);
		return (int) years;
	}

	public static List<Kontakt> findAllPatients() {
		JPAQuery<Kontakt> query = new JPAQuery<Kontakt>(Kontakt.class);
		query.add(Kontakt_.person, QUERY.EQUALS, true);
		query.add(Kontakt_.patient, QUERY.EQUALS, true);
		return query.execute();
	}

	public static Optional<Kontakt> findPatientByPatientNumber(int randomPatientNumber) {
		JPAQuery<Kontakt> query = new JPAQuery<Kontakt>(Kontakt.class);
		query.add(Kontakt_.person, QUERY.EQUALS, true);
		query.add(Kontakt_.patient, QUERY.EQUALS, true);
		query.add(Kontakt_.code, QUERY.EQUALS, Integer.toString(randomPatientNumber));
		return query.executeGetSingleResult();
	}

	public static Optional<IContact> findLaboratory(String identifier) {
		if (identifier == null || identifier.isEmpty()) {
			throw new IllegalArgumentException("Labor identifier [" + identifier + "] invalid.");
		}

		IContact laboratory = null;
		JPAQuery<Kontakt> query = new JPAQuery<Kontakt>(Kontakt.class);
		query.add(Kontakt_.code, QUERY.LIKE, "%" + identifier + "%");
		query.or(Kontakt_.description1, QUERY.LIKE, "%" + identifier + "%");
		List<Kontakt> results = query.execute();
		if (results.isEmpty()) {
			log.warn("Found no Labor for identifier [" + identifier + "]. Created new Labor contact.");
			return Optional.empty();
		} else {
			laboratory = results.get(0);
			if (results.size() > 1) {
				log.warn("Found more than one Labor for identifier [" + identifier
						+ "]. This can cause problems when importing results.");
			}
		}
		return Optional.ofNullable(laboratory);
	}

	// TODO normalize
	public static List<Kontakt> findPersonByMultipleOptionalParameters(String firstName, String lastName, Gender gender,
			String city, String street, String zip) {
		JPAQuery<Kontakt> query = new JPAQuery<Kontakt>(Kontakt.class);
		query.add(Kontakt_.person, QUERY.EQUALS, true);
		if (firstName != null) {
			// TODO TEST
			query.addLikeNormalized(Kontakt_.description1, "%" + firstName.trim() + "%");
		}
		if (lastName != null) {
			query.add(Kontakt_.description2, QUERY.LIKE, "%" + lastName.trim() + "%");
		}
		if (gender != null) {
			query.add(Kontakt_.gender, QUERY.EQUALS, gender);
		}
		if (city != null) {
			query.add(Kontakt_.city, QUERY.LIKE, "%" + city.trim() + "%");
		}
		if (street != null) {
			query.add(Kontakt_.street, QUERY.LIKE, "%" + street.trim() + "%");
		}
		if (zip != null) {
			query.add(Kontakt_.zip, QUERY.LIKE, "%" + zip.trim() + "%");
		}
		return query.execute();
	}
}
