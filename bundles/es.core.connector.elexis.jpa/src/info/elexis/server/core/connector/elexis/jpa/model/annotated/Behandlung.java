package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Convert;

import ch.rgw.tools.VersionedResource;

@Entity
@Table(name = "behandlungen")
public class Behandlung extends AbstractDBObjectIdDeleted {

	@OneToOne
	@JoinColumn(name = "fallId")
	private Fall fall;

	@OneToOne
	@JoinColumn(name = "mandantId")
	private Kontakt mandant;

	@OneToOne
	@JoinColumn(name = "RechnungsID")
	private Invoice invoice;

	@Column(length = 8)
	private LocalDate datum;

	@OneToMany
	@JoinTable(name = "behdl_dg_joint", joinColumns = @JoinColumn(name = "BehandlungsID"), inverseJoinColumns = @JoinColumn(name = "DiagnoseID"))
	private Set<Diagnosis> diagnoses;

	/**
	 * Seems to be always null
	 */
	@Column(length = 25, name = "leistungen")
	private String leistungenId;

	@Basic(fetch = FetchType.LAZY)
	@Convert(value = "VersionedResourceConverter")
	private VersionedResource eintrag;

	@Convert("booleanStringConverter")
	@Column(length = 1)
	private boolean billable = true; // DBModel 3.7.0

	@Column(name = "zeit", length = 6)
	private LocalTime time; // DBModel 3.7.0

	public Fall getFall() {
		return fall;
	}

	public void setFall(Fall fall) {
		this.fall = fall;
	}

	public Kontakt getMandant() {
		return mandant;
	}

	public void setMandant(Kontakt mandant) {
		this.mandant = mandant;
	}

	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}

	/**
	 * @return date if value is set, else <code>null</code>
	 */
	public LocalDate getDatum() {
		return datum;
	}

	public void setDatum(LocalDate datum) {
		this.datum = datum;
	}

	public Set<Diagnosis> getDiagnoses() {
		return diagnoses;
	}

	public void setDiagnoses(Set<Diagnosis> diagnoses) {
		this.diagnoses = diagnoses;
	}

	public String getLeistungenId() {
		return leistungenId;
	}

	public void setLeistungenId(String leistungenId) {
		this.leistungenId = leistungenId;
	}

	public VersionedResource getEintrag() {
		if (eintrag == null) {
			eintrag = VersionedResource.load(null);
		}
		return eintrag;
	}

	public void setEintrag(VersionedResource eintrag) {
		this.eintrag = eintrag;
	}

	public boolean isBillable() {
		return billable;
	}

	public void setBillable(boolean billable) {
		this.billable = billable;
	}

	public LocalTime getTime() {
		return time;
	}

	public void setTime(LocalTime time) {
		this.time = time;
	}

	/**
	 * 
	 * @return the local date and time of a consultation. If the consultation does
	 *         not bear time information, it is defaulted to midnight
	 * @since 1.7
	 */
	public LocalDateTime getDateTime() {
		return LocalDateTime.of(getDatum(), getTime());
	}

	/**
	 * @param dateTime
	 * @since 1.7
	 */
	public void setDateTime(LocalDateTime dateTime) {
		setDatum(dateTime.toLocalDate());
		setTime(dateTime.toLocalTime());
	}
	
	@Override
	public String getLabel() {
		return getDatum() + " " + getMandant() != null ? getMandant().getLabel() : "";
	}

	@Override
	public String toString() {
		return super.toString() + "datum=[" + getDatum() + "] mandant=[" + getMandant() + "]";
	}
}
