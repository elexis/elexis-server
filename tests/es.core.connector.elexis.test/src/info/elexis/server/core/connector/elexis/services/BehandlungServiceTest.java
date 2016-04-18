package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.elexis.core.types.Gender;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Diagnosis;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

public class BehandlungServiceTest {

	Kontakt patient;
	Behandlung cons;

	@Before
	public void initialize() {
		patient = KontaktService.INSTANCE.createPatient("FirstName", "LastName", LocalDate.now(), Gender.MALE);
	}

	@After
	public void cleanup() {

		KontaktService.INSTANCE.remove(patient);

	}

	@Test
	@Ignore
	public void testGetAllConsultationsForPatient() {
		Kontakt myPatient = KontaktService.INSTANCE.findById("z7562af3f31f535503455").get();
		List<Behandlung> consultations = BehandlungService.findAllConsultationsForPatient(myPatient);
		assertTrue(consultations.size() > 0);
		assertTrue(consultations.get(0).getDatum().isAfter(consultations.get(1).getDatum()));
	}

	@Test
	public void testSetAndGetDiagnosesForConsultation() {
		cons = BehandlungService.INSTANCE.create();
		cons.setDatum(LocalDate.now());

		Diagnosis d = new Diagnosis();
		d.setCode("testCode");
		d.setDeleted(false);
		d.setText("blaText");

		BehandlungService.INSTANCE.setDiagnosisOnConsultation(cons, d);
		BehandlungService.INSTANCE.setDiagnosisOnConsultation(cons, d);
		
		BehandlungService.INSTANCE.flush();

		Optional<Behandlung> storedCons = BehandlungService.INSTANCE.findById(cons.getId());

		assertEquals(1, storedCons.get().getDiagnoses().size());
		BehandlungService.INSTANCE.remove(cons);
	}

}
