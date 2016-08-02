package info.elexis.server.core.connector.elexis.services;

import java.util.ArrayList;
import java.util.List;

import ch.elexis.core.types.Gender;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;

public abstract class AbstractServiceTest {

	public List<Kontakt> testContacts = new ArrayList<Kontakt>();
	public List<Kontakt> testPatients = new ArrayList<Kontakt>();
	public List<Fall> testFaelle = new ArrayList<Fall>();
	public List<Behandlung> testBehandlungen = new ArrayList<Behandlung>();

	public void createTestMandantPatientFallBehandlung() {
		Kontakt mandator = KontaktService.INSTANCE.create();
		TimeTool timeTool = new TimeTool();
		mandator.setDescription1("mandator1 " + timeTool.toString());
		mandator.setDescription2("description2");
		mandator.setDescription3("description3");
		mandator.setMandator(true);
		mandator.setDateOfBirth(timeTool);
		testContacts.add(mandator);
		KontaktService.INSTANCE.flush();

		Kontakt patient = KontaktService.INSTANCE.createPatient("Armer", "Anton" + timeTool.toString(),
				timeTool.toLocalDate(), Gender.MALE);
		testPatients.add(patient);

		Fall testFall = FallService.INSTANCE.create(patient, "Fallbezeichnung", "Fallgrund", "KVG");
		FallService.INSTANCE.flush();
		testFaelle.add(testFall);

		Behandlung behandlung = BehandlungService.INSTANCE.create(testFall, mandator);
		BehandlungService.INSTANCE.flush();
		testBehandlungen.add(behandlung);
	}

	public void cleanup() {
		for (Behandlung cons : testBehandlungen) {
			List<Verrechnet> verrechnet = cons.getVerrechnet();
			for (Verrechnet verrechnet2 : verrechnet) {
				System.out
						.println("Deleting verrechnet " + verrechnet2.getLabel() + " on behandlung " + cons.getLabel());
				VerrechnetService.INSTANCE.remove(verrechnet2);
			}

			System.out.println("Deleting behandlung " + cons.getLabel());
			BehandlungService.INSTANCE.remove(cons);
		}
		for (Fall fall : testFaelle) {
			System.out.println("Deleting fall " + fall.getLabel());
			FallService.INSTANCE.remove(fall);
		}
		for (Kontakt contact : testPatients) {
			System.out.println("Deleting patient " + contact.getLabel());
			KontaktService.INSTANCE.remove(contact);
		}
		for (Kontakt contact : testContacts) {
			System.out.println("Deleting contact " + contact.getLabel());
			KontaktService.INSTANCE.remove(contact);
		}
	}

}
