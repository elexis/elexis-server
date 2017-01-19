package info.elexis.server.core.connector.elexis.services;

import java.util.Optional;
import java.util.Set;

import ch.elexis.core.model.issue.Priority;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Type;
import ch.elexis.core.model.issue.Visibility;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Reminder;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ReminderResponsible;

public class ReminderService extends PersistenceService {

	public static class Builder extends AbstractBuilder<Reminder> {
		public Builder(Kontakt creator, final Visibility visibility, final String subject) {
			object = new Reminder();
			object.setCreator(creator);
			object.setKontakt(creator);
			object.setVisibility(visibility);
			object.setSubject(subject);
			object.setPriority(Priority.MEDIUM);
			object.setActionType(Type.COMMON);
			object.setStatus(ProcessStatus.OPEN);
		}
	}

	/**
	 * convenience method
	 * 
	 * @param id
	 * @return
	 */
	public static Optional<Reminder> load(String id) {
		return PersistenceService.load(Reminder.class, id).map(v -> (Reminder) v);
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
