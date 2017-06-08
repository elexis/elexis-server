package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "REMINDERS_RESPONSIBLE_LINK")
public class ReminderResponsible extends AbstractDBObjectIdDeleted {

	@ManyToOne
	@JoinColumn(name = "ReminderID", nullable = false)
	private Reminder reminder;

	@ManyToOne
	@JoinColumn(name = "ResponsibleID", nullable = false)
	private Kontakt responsible;

	public Reminder getReminder() {
		return reminder;
	}

	public void setReminder(Reminder reminder) {
		this.reminder = reminder;
	}

	public Kontakt getResponsible() {
		return responsible;
	}

	public void setResponsible(Kontakt responsible) {
		this.responsible = responsible;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() == this.getClass()) {
			ReminderResponsible b = (ReminderResponsible) obj;
			Reminder bReminder = b.getReminder();
			Kontakt bResponsible = b.getResponsible();

			if (reminder != null && responsible != null) {
				if (bReminder != null && bResponsible != null) {
					String reminderId = reminder.getId();
					String responsibleId = responsible.getId();
					boolean equalReminder = reminderId.equals(bReminder.getId());
					boolean equalResponsible = responsibleId.equals(bResponsible.getId());
					return (equalReminder && equalResponsible);
				}
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		int reminderHash = (reminder != null && reminder.getId() != null) ? reminder.getId().hashCode() : 0;
		int reponsibleHash = (responsible != null && responsible.getId() != null) ? responsible.getId().hashCode() : 0;

		return reminderHash + reponsibleHash;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}
}
