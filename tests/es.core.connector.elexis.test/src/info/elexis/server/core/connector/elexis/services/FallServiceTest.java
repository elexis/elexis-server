package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.FallConstants;
import ch.elexis.core.types.Gender;
import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

public class FallServiceTest {

	Kontakt patient;
	Optional<Kontakt> patientO = Optional.empty();
	Fall fall;

	@Before
	public void initialize() {
		patient = KontaktService.INSTANCE.createPatient("FirstName", "LastName", LocalDate.now(), Gender.MALE);
	}
	
	@After
	public void cleanup() {
		FallService.INSTANCE.remove(fall);
		KontaktService.INSTANCE.remove(patient);
	}

	@Test
	public void testCreateKontaktStringStringString() {
		fall = FallService.INSTANCE.create(patient, "test", FallConstants.TYPE_DISEASE, "UVG");

		EntityManager em = ElexisEntityManager.createEntityManager();
		Fall storedFall = em.find(Fall.class, fall.getId());
		assertEquals(fall.getPatientKontakt().getId(), storedFall.getPatientKontakt().getId());
		assertEquals(fall.getBezeichnung(), storedFall.getBezeichnung());
		assertEquals(fall.getGesetz(), storedFall.getGesetz());
		assertEquals("UVG", storedFall.getExtInfo().get(FallConstants.FLD_EXTINFO_BILLING));
		em.close();
	}
}
