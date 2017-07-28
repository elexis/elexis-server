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
		TimeTool timeTool = new TimeTool();
		Kontakt mandator = new KontaktService.PersonBuilder("mandator1 " + timeTool.toString(),
				"Anton" + timeTool.toString(), timeTool.toLocalDate(), Gender.MALE).mandator().buildAndSave();
		testContacts.add(mandator);

		Kontakt patient = new KontaktService.PersonBuilder("Armer", "Anton" + timeTool.toString(),
				timeTool.toLocalDate(), Gender.MALE).buildAndSave();
		testPatients.add(patient);

		Fall testFall = new FallService.Builder(patient, "Fallbezeichnung", "Fallgrund", "KVG").buildAndSave();
		testFaelle.add(testFall);

		Behandlung behandlung = new BehandlungService.Builder(testFall, mandator).buildAndSave();
		testBehandlungen.add(behandlung);
	}

	public void cleanup() {
		for (Behandlung cons : testBehandlungen) {
			List<Verrechnet> verrechnet = VerrechnetService.getAllVerrechnetForBehandlung(cons);
			for (Verrechnet verrechnet2 : verrechnet) {
				System.out
						.print("Deleting verrechnet " + verrechnet2.getLabel() + " on behandlung " + cons.getLabel());
				VerrechnetService.remove(verrechnet2);
				System.out.println(" [OK]");
			}

			System.out.print("Deleting behandlung " + cons.getLabel());
			BehandlungService.remove(cons);
			System.out.println(" [OK]");
		}
		for (Fall fall : testFaelle) {
			System.out.print("Removing fall " + fall.getLabel());
			FallService.remove(fall);
			System.out.println(" [OK]");
		}
		for (Kontakt contact : testPatients) {
			System.out.print("Removing patient " + contact.getLabel());
			PersistenceService.remove(contact);
			System.out.println(" [OK]");
		}
		for (Kontakt contact : testContacts) {
			System.out.print("Removing contact " + contact.getLabel());
			PersistenceService.remove(contact);
			System.out.println(" [OK]");
		}
	}

}
