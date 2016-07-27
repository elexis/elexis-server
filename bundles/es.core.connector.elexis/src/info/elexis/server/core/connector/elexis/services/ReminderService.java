package info.elexis.server.core.connector.elexis.services;

import java.util.Set;

import ch.elexis.core.model.issue.Priority;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Type;
import ch.elexis.core.model.issue.Visibility;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Reminder;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ReminderResponsible;

public class ReminderService extends AbstractService<Reminder> {

	public static ReminderService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final ReminderService INSTANCE = new ReminderService();
	}

	public ReminderService() {
		super(Reminder.class);
	}

	public Reminder create(Kontakt creator, final Visibility visibility, final String subject) {
		em.getTransaction().begin();
		Reminder reminder = create(false);
		em.merge(creator);
		reminder.setCreator(creator);
		reminder.setKontakt(creator);
		reminder.setVisibility(visibility);
		reminder.setSubject(subject);
		reminder.setPriority(Priority.MEDIUM);
		reminder.setActionType(Type.COMMON);
		reminder.setStatus(ProcessStatus.OPEN);
		em.getTransaction().commit();
		return reminder;
	}

	/**
	 * Adds or removes a responsible user contact to the reminder.
	 * 
	 * @param reminder
	 * @param contact
	 * @param add
	 *            <code>true</code> to add, <code>false</code> to remove
	 */
	public static void addOrRemoveResponsibleReminderContact(Reminder reminder, Kontakt contact, boolean add) {
		ReminderResponsible rr = new ReminderResponsible();
		rr.setReminder(reminder);
		rr.setResponsible(contact);

		Set<ReminderResponsible> responsible = reminder.getResponsible();
		if (!add && responsible.contains(rr)) {
			responsible.remove(rr);
		} else {
			responsible.add(rr);
		}
	}

}
