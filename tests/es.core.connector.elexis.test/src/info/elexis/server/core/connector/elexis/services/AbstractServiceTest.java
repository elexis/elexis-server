package info.elexis.server.core.connector.elexis.services;

import java.util.ArrayList;
import java.util.List;

import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

public abstract class AbstractServiceTest {

	public static List<Kontakt> testContacts = new ArrayList<Kontakt>();

	public static void createTestKontakt() {
		Kontakt testContact = KontaktService.INSTANCE.create();
		TimeTool timeTool = new TimeTool();
		testContact.setDescription1("description1 " + timeTool.toString());
		testContact.setDescription2("description2");
		testContact.setDescription3("description3");
		testContact.setDateOfBirth(timeTool);
		testContacts.add(testContact);
		KontaktService.INSTANCE.flush();
		System.out.println("Created contact " + testContact.getLabel());
	}

	public static void deleteTestKontakt() {
		for (Kontakt contact : testContacts) {
			System.out.println("Deleting contact " + contact.getLabel());
			KontaktService.INSTANCE.remove(contact);
		}
	}

}
