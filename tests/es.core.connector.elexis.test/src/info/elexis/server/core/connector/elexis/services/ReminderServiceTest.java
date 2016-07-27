package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.model.issue.Priority;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Type;
import ch.elexis.core.model.issue.Visibility;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Reminder;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ReminderResponsible;

public class ReminderServiceTest extends AbstractServiceTest {

	@BeforeClass
	public static void beforeClass() {
		createTestKontakt();
	}

	@AfterClass
	public static void afterClass() {
		deleteTestKontakt();
	}
	
	@Test
	public void testCreateAndDeleteReminder() throws InstantiationException, IllegalAccessException {
		Reminder reminder = ReminderService.INSTANCE.create(testContacts.get(0), Visibility.ALWAYS, "testSubject");		
		reminder.setStatus(ProcessStatus.CLOSED);
		ReminderResponsible rr = new ReminderResponsible();
		rr.setReminder(reminder);
		rr.setResponsible(testContacts.get(0));
		reminder.getResponsible().add(rr);
		ReminderService.INSTANCE.flush();
		
		Reminder findById = ReminderService.INSTANCE.findById(reminder.getId()).get();
		
		assertEquals(reminder.getId(), findById.getId());
		assertEquals(testContacts.get(0), findById.getCreator());
		assertEquals(testContacts.get(0), findById.getKontakt());
		assertEquals(Visibility.ALWAYS, findById.getVisibility());
		assertEquals("testSubject", findById.getSubject());
		assertEquals(Priority.MEDIUM, findById.getPriority());
		assertEquals(Type.COMMON, findById.getActionType());
		assertEquals(ProcessStatus.CLOSED, findById.getStatus());
		assertEquals(1, findById.getResponsible().size());
		
		ReminderResponsible rr2 = new ReminderResponsible();
		rr2.setReminder(findById);
		rr2.setResponsible(testContacts.get(0));
		assertEquals(true, findById.getResponsible().contains(rr));
		
		ReminderService.INSTANCE.remove(findById);
		Optional<Reminder> found = ReminderService.INSTANCE.findById(reminder.getId());
		assertFalse(found.isPresent());
	}
	
	@Test
	public void testCreateAndDeleteReminderResponsibles()  {
		Reminder reminder = ReminderService.INSTANCE.create(testContacts.get(0), Visibility.ALWAYS, "testSubject");		
		reminder.setStatus(ProcessStatus.CLOSED);
		ReminderResponsible rr = new ReminderResponsible();
		rr.setReminder(reminder);
		rr.setResponsible(testContacts.get(0));
		reminder.getResponsible().add(rr);
		ReminderService.INSTANCE.flush();
		
		createTestKontakt();
		
		reminder.getResponsible().clear(); // clear the list, although one already inserted
		
		ReminderService.addOrRemoveResponsibleReminderContact(reminder, testContacts.get(1), true);
		ReminderService.addOrRemoveResponsibleReminderContact(reminder, testContacts.get(1), true);
		ReminderService.addOrRemoveResponsibleReminderContact(reminder, testContacts.get(1), true);
		
		createTestKontakt();
		
		ReminderService.addOrRemoveResponsibleReminderContact(reminder, testContacts.get(2), true);
		
		ReminderService.addOrRemoveResponsibleReminderContact(reminder, testContacts.get(1), false);
		
		Reminder findById = ReminderService.INSTANCE.findById(reminder.getId()).get();
	
		assertEquals(1, findById.getResponsible().size());
		
		ReminderService.INSTANCE.remove(findById);
		Optional<Reminder> found = ReminderService.INSTANCE.findById(reminder.getId());
		assertFalse(found.isPresent());
	}
}
