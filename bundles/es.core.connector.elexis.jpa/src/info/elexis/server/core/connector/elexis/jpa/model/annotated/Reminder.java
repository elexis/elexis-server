package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.time.LocalDate;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.ReadTransformer;
import org.eclipse.persistence.annotations.WriteTransformer;

import ch.elexis.core.model.ReminderConstants.Status;
import ch.elexis.core.model.ReminderConstants.Typ;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.transformer.ElexisDBStringDateTransformer;

@Entity
@Table(name = "reminders")
public class Reminder extends AbstractDBObjectIdDeleted {

	@OneToOne
	@JoinColumn(name = "IdentID")
	private Kontakt kontakt;

	@OneToOne
	@JoinColumn(name = "OriginID")
	private Kontakt creator;

	/**
	 * seems like this field is not used
	 */
	@OneToOne
	@JoinColumn
	private Kontakt responsibleOld;

	@OneToMany
	@JoinTable(name = "reminders_responsible_link", joinColumns = @JoinColumn(name = "ReminderID") , inverseJoinColumns = @JoinColumn(name = "ResponsibleID") )
	private Collection<Kontakt> responsible;

	@ReadTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	@WriteTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	protected LocalDate dateDue;

	@Enumerated(EnumType.ORDINAL)
	protected Status status;

	@Enumerated(EnumType.ORDINAL)
	protected Typ typ;

	@Lob()
	protected String params;

	@Lob()
	protected String message;

	public Kontakt getKontakt() {
		return kontakt;
	}

	public void setKontakt(Kontakt kontakt) {
		this.kontakt = kontakt;
	}

	public Kontakt getCreator() {
		return creator;
	}

	public void setCreator(Kontakt creator) {
		this.creator = creator;
	}

	public LocalDate getDateDue() {
		return dateDue;
	}

	public void setDateDue(LocalDate dateDue) {
		this.dateDue = dateDue;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Typ getTyp() {
		return typ;
	}

	public void setTyp(Typ typ) {
		this.typ = typ;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Collection<Kontakt> getResponsible() {
		return responsible;
	}

	public Kontakt getResponsibleOld() {
		return responsibleOld;
	}

	public void setResponsible(Collection<Kontakt> responsible) {
		this.responsible = responsible;
	}

	public void setResponsibleOld(Kontakt responsibleOld) {
		this.responsibleOld = responsibleOld;
	}
}
