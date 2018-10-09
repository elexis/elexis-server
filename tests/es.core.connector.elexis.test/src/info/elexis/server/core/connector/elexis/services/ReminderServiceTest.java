//package info.elexis.server.core.connector.elexis.services;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.Set;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import ch.elexis.core.model.issue.Priority;
//import ch.elexis.core.model.issue.ProcessStatus;
//import ch.elexis.core.model.issue.Type;
//import ch.elexis.core.model.issue.Visibility;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Reminder;
//
//public class ReminderServiceTest extends AbstractServiceTest {
//
//	@Before
//	public void beforeClass() {
//		createTestMandantPatientFallBehandlung();
//	}
//
//	@After
//	public void afterClass() {
//		cleanup();
//	}
//
//	@Test
//	public void testLoadExistingReminder() {
//		Reminder reminder = ReminderService.load("i86a54e5b46e66bda01308").get();
//		Kontakt contact = KontaktService.load("h2c1172107ce2df95065").get();
//		Kontakt testPatient = KontaktService.load("s9b71824bf6b877701111").get();
//		assertEquals(reminder.getId(), reminder.getId());
//		assertEquals(contact, reminder.getCreator());
//		assertEquals(testPatient, reminder.getKontakt());
//		assertEquals(Visibility.POPUP_ON_PATIENT_SELECTION, reminder.getVisibility());
//		assertEquals("Assura", reminder.getSubject());
//		assertEquals(Priority.MEDIUM, reminder.getPriority());
//		assertEquals(Type.COMMON, reminder.getActionType());
//		assertEquals(ProcessStatus.OVERDUE, reminder.getStatus());
//		assertEquals(1, reminder.getResponsible().size());
//		assertEquals(true, reminder.getResponsible().contains(contact));
//	}
//
//	@Test
//	public void testCreateAndDeleteReminder() throws InstantiationException, IllegalAccessException {
//		Reminder reminder = new ReminderService.Builder(testContacts.get(0), Visibility.ALWAYS, "testSubject")
//				.buildAndSave();
//		reminder.setStatus(ProcessStatus.CLOSED);
//		reminder.setResponsible(Collections.singleton(testContacts.get(0)));
//		ReminderService.save(reminder);
//
//		Reminder findById = ReminderService.load(reminder.getId()).get();
//
//		assertEquals(reminder.getId(), findById.getId());
//		assertEquals(testContacts.get(0), findById.getCreator());
//		assertEquals(testContacts.get(0), findById.getKontakt());
//		assertEquals(Visibility.ALWAYS, findById.getVisibility());
//		assertEquals("testSubject", findById.getSubject());
//		assertEquals(Priority.MEDIUM, findById.getPriority());
//		assertEquals(Type.COMMON, findById.getActionType());
//		assertEquals(ProcessStatus.CLOSED, findById.getStatus());
//		assertEquals(1, findById.getResponsible().size());
//		assertEquals(true, findById.getResponsible().contains(testContacts.get(0)));
//
//		ReminderService.remove(findById);
//		assertFalse(ReminderService.load(reminder.getId()).isPresent());
//	}
//
//	@Test
//	public void testCreateAndDeleteReminderResponsibles() {
//		Reminder reminder = new ReminderService.Builder(testContacts.get(0), Visibility.ALWAYS, "testSubject")
//				.buildAndSave();
//		reminder.setStatus(ProcessStatus.CLOSED);
//		reminder.setResponsible(Collections.singleton(testContacts.get(0)));
//
//		createTestMandantPatientFallBehandlung();
//
//		Set<Kontakt> responsibles = new HashSet<>();
//		responsibles.add(testContacts.get(0));
//		responsibles.add(testContacts.get(1));
//		reminder.setResponsible(responsibles);
//		ReminderService.save(reminder);
//
//		Reminder findById = ReminderService.load(reminder.getId()).get();
//		assertEquals(2, findById.getResponsible().size());
//
//		createTestMandantPatientFallBehandlung();
//
//		responsibles.add(testContacts.get(2));
//		ReminderService.save(reminder);
//
//		findById = ReminderService.load(reminder.getId()).get();
//		assertEquals(3, findById.getResponsible().size());
//
//		ReminderService.remove(findById);
//		assertFalse(ReminderService.load(reminder.getId()).isPresent());
//	}
//}
