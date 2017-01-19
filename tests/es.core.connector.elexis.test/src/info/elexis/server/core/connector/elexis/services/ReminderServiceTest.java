package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.issue.Priority;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Type;
import ch.elexis.core.model.issue.Visibility;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Reminder;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ReminderResponsible;

public class ReminderServiceTest extends AbstractServiceTest {

	@Before
	public void beforeClass() {
		createTestMandantPatientFallBehandlung();
	}

	@After
	public void afterClass() {
		cleanup();
	}

	@Test
	public void testCreateAndDeleteReminder() throws InstantiationException, IllegalAccessException {
		Reminder reminder = new ReminderService.Builder(testContacts.get(0), Visibility.ALWAYS, "testSubject")
				.buildAndSave();
		reminder.setStatus(ProcessStatus.CLOSED);
		ReminderResponsible rr = new ReminderResponsible();
		rr.setReminder(reminder);
		rr.setResponsible(testContacts.get(0));
		reminder.getResponsible().add(rr);
		ReminderService.save(reminder);

		Reminder findById = ReminderService.load(reminder.getId()).get();

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

		ReminderService.remove(findById);
		Optional<Reminder> found = ReminderService.load(reminder.getId());
		assertFalse(found.isPresent());
	}

	@Test
	public void testCreateAndDeleteReminderResponsibles() {
		Reminder reminder = new ReminderService.Builder(testContacts.get(0), Visibility.ALWAYS, "testSubject")
				.buildAndSave();
		reminder.setStatus(ProcessStatus.CLOSED);
		ReminderResponsible rr = new ReminderResponsible();
		rr.setReminder(reminder);
		rr.setResponsible(testContacts.get(0));
		reminder.getResponsible().add(rr);
		ReminderService.save(rr);

		createTestMandantPatientFallBehandlung();

		reminder.getResponsible().clear(); // clear the list, although one
											// already inserted

		ReminderService.addOrRemoveResponsibleReminderContact(reminder, testContacts.get(1), true);
		ReminderService.addOrRemoveResponsibleReminderContact(reminder, testContacts.get(1), true);
		ReminderService.addOrRemoveResponsibleReminderContact(reminder, testContacts.get(1), true);

		createTestMandantPatientFallBehandlung();

		ReminderService.addOrRemoveResponsibleReminderContact(reminder, testContacts.get(2), true);

		ReminderService.addOrRemoveResponsibleReminderContact(reminder, testContacts.get(1), false);

		Reminder findById = ReminderService.load(reminder.getId()).get();

		assertEquals(1, findById.getResponsible().size());

		ReminderService.remove(findById);
		Optional<Reminder> found = ReminderService.load(reminder.getId());
		assertFalse(found.isPresent());
	}
}
