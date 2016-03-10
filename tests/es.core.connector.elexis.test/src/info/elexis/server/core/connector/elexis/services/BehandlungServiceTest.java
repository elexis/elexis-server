package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

public class BehandlungServiceTest {

	Kontakt patient;
	Optional<Kontakt> patientO = Optional.empty();

	@Before
	public void initialize() {
		while (!patientO.isPresent()) {
			int randomPatientNumber = new Random().nextInt(500) + 100;
			System.out.println("Trying to find random patient " + randomPatientNumber);
			patientO = KontaktService.findPatientByPatientNumber(randomPatientNumber);
		}
		patient = patientO.get();
	}

	@Test
	public void testGetAllConsultationsForPatient() {
		Kontakt myPatient = KontaktService.INSTANCE.findById("z7562af3f31f535503455");
		List<Behandlung> consultations = BehandlungService.findAllConsultationsForPatient(myPatient);
		assertTrue(consultations.size() > 0);
		assertTrue(consultations.get(0).getDatum().isAfter(consultations.get(1).getDatum()));
	}


}
