package info.elexis.server.core.connector.elexis.services;

import java.util.ArrayList;
import java.util.List;

public class StressTest {

	private static int upperBound = 100;
	private static List<String> patients = new ArrayList<String>();

	// @Test
	// public void stressTest() throws InterruptedException {
	//
	// List<String> faelle = new ArrayList<String>();
	// List<String> consultations = new ArrayList<String>();
	//
	// Kontakt mandator = KontaktService.INSTANCE.createPatient("Mandator",
	// "Mandator", LocalDate.now(), Gender.MALE);
	//
	// for (int i = 0; i < upperBound; i++) {
	// Kontakt patient = KontaktService.INSTANCE.createPatient("FirstName" + i,
	// "LastName" + i, LocalDate.now(),
	// Gender.FEMALE);
	// patients.add(patient.getId());
	//
	// KontaktService.INSTANCE.flush();
	//
	// Fall fall = FallService.INSTANCE.create(patient, "label" + i, "reason" +
	// i, "billingMethod" + i);
	// faelle.add(fall.getId());
	//
	// FallService.INSTANCE.flush();
	//
	// Behandlung behandlung = BehandlungService.INSTANCE.create(fall,
	// mandator);
	// consultations.add(behandlung.getId());
	//
	// BehandlungService.INSTANCE.flush();
	//
	// System.out.println("Create " + i + "....");
	// }
	//
	// Thread t1 = new Thread(new ModifyRunnable("Thread1"));
	// t1.start();
	// Thread.sleep(500);
	//// Thread t2 = new Thread(new ModifyRunnable("Thread2"));
	//// t2.start();
	//
	// t1.join();
	//// t2.join();
	//
	// for (String string : consultations) {
	// Optional<Behandlung> findById =
	// BehandlungService.INSTANCE.findById(string);
	// BehandlungService.INSTANCE.remove(findById.get());
	// BehandlungService.INSTANCE.flush();
	// System.out.println("Delete consultation...");
	// }
	//
	// for (String string : faelle) {
	// Optional<Fall> findById = FallService.INSTANCE.findById(string);
	// FallService.INSTANCE.remove(findById.get());
	// FallService.INSTANCE.flush();
	// System.out.println("Delete fall...");
	// }
	//
	// for (String string : patients) {
	// Optional<Kontakt> findById = KontaktService.INSTANCE.findById(string);
	// KontaktService.INSTANCE.remove(findById.get());
	// KontaktService.INSTANCE.flush();
	// System.out.println("Delete patient...");
	// }
	// }
	//
	// private class ModifyRunnable implements Runnable {
	// private final String id;
	//
	// public ModifyRunnable(String string) {
	// this.id = string;
	// }
	//
	// @Override
	// public void run() {
	// int i = 0;
	// for (String string : patients) {
	// Optional<Kontakt> findById = KontaktService.INSTANCE.findById(string);
	// findById.get().setAllergies("allergies"+id+i);
	// findById.get().setCity("city"+id+i);
	// KontaktService.INSTANCE.flush();
	// }
	// i++;
	// }
	//
	// }
}
