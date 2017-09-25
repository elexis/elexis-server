package info.elexis.server.core.connector.elexis.services;

import java.util.Optional;

import ch.elexis.core.model.issue.Priority;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Type;
import ch.elexis.core.model.issue.Visibility;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Reminder;

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

}
