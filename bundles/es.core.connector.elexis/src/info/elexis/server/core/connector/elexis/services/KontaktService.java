package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import ch.elexis.core.model.IContact;
import ch.elexis.core.types.Gender;
import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Config;
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

	private static final DateTimeFormatter sdf = DateTimeFormatter.ofPattern("dd.MM.yyyy");

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

	private IContact createLaboratory(String identifier, String name) {
		em.getTransaction().begin();
		Kontakt laboratory = create(false);
		laboratory.setDescription1(name);
		laboratory.setDescription2("Labor");
		laboratory.setCode(identifier);
		laboratory.setOrganisation(true);
		laboratory.setLaboratory(true);
		em.getTransaction().commit();
		return laboratory;
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
		LocalDate dob = k.getDob();
		if (dob == null) {
			return -1;
		}

		LocalDate now = LocalDate.now();
		long years = ChronoUnit.YEARS.between(dob, now);
		return (int) years;
	}

	public List<Kontakt> findAllPatients() {
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

	public Optional<IContact> findLaboratory(String identifier) {
		if (identifier == null || identifier.isEmpty()) {
			throw new IllegalArgumentException("Labor identifier [" + identifier + "] invalid.");
		}
		
		IContact laboratory = null;	
		JPAQuery<Kontakt> query = new JPAQuery<Kontakt>(Kontakt.class);
		query.add(Kontakt_.code, QUERY.LIKE,  "%" + identifier + "%");
		query.or(Kontakt_.description1, QUERY.LIKE,  "%" + identifier + "%");
		List<Kontakt> results = query.execute();
		if (results.isEmpty()) {
			log.warn(
					"Found no Labor for identifier [" + identifier + "]. Created new Labor contact.");
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
}
