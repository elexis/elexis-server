package info.elexis.server.core.connector.elexis.services;

import java.util.ArrayList;
import java.util.List;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.ICoverageBuilder;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.rgw.tools.TimeTool;

public abstract class AbstractServiceTest {
	
	public IModelService coreModelService = OsgiServiceUtil.getService(IModelService.class,
		"(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();
	
	public List<IContact> testContacts = new ArrayList<IContact>();
	public List<IContact> testPatients = new ArrayList<IContact>();
	public List<ICoverage> testFaelle = new ArrayList<ICoverage>();
	public List<IEncounter> testBehandlungen = new ArrayList<IEncounter>();
	
	public void createTestMandantPatientFallBehandlung(){
		TimeTool timeTool = new TimeTool();
		IPerson mandator =
			new IContactBuilder.PersonBuilder(coreModelService, "mandator1 " + timeTool.toString(),
				"Anton" + timeTool.toString(), timeTool.toLocalDate(), Gender.MALE).mandator()
					.buildAndSave();
		mandator.setMandator(true);
		testContacts.add(mandator);
		
		IPatient patient = new IContactBuilder.PatientBuilder(coreModelService, "Armer",
			"Anton" + timeTool.toString(), timeTool.toLocalDate(), Gender.MALE).buildAndSave();
		testPatients.add(patient);
		
		ICoverage testFall =
			new ICoverageBuilder(coreModelService, patient, "Fallbezeichnung", "Fallgrund", "KVG")
				.buildAndSave();
		testFaelle.add(testFall);
		
		IEncounter behandlung =
			new IEncounterBuilder(coreModelService, testFall, (IMandator) mandator).buildAndSave();
		testBehandlungen.add(behandlung);
	}
	
	public void cleanup(){
		for (IEncounter cons : testBehandlungen) {
			//			List<Verrechnet> verrechnet = VerrechnetService.getAllVerrechnetForBehandlung(cons);
			//			for (Verrechnet verrechnet2 : verrechnet) {
			//				System.out.print("Deleting verrechnet " + verrechnet2.getLabel() + " on behandlung "
			//					+ cons.getLabel());
			//				VerrechnetService.remove(verrechnet2);
			//				System.out.println(" [OK]");
			//			}
			
			System.out.print("Deleting behandlung " + cons.getLabel());
			coreModelService.remove(cons);
			System.out.println(" [OK]");
		}
		for (ICoverage fall : testFaelle) {
			System.out.print("Removing fall " + fall.getLabel());
			coreModelService.remove(fall);
			System.out.println(" [OK]");
		}
		for (IContact contact : testPatients) {
			System.out.print("Removing patient " + contact.getLabel());
			coreModelService.remove(contact);
			System.out.println(" [OK]");
		}
		for (IContact contact : testContacts) {
			System.out.print("Removing contact " + contact.getLabel());
			coreModelService.remove(contact);
			System.out.println(" [OK]");
		}
	}
	
}
