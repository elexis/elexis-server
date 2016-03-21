package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;

import ch.rgw.tools.VersionedResource;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.converter.ElexisDBStringDateConverter;

@Entity
@Table(name = "behandlungen")
public class Behandlung extends AbstractDBObjectIdDeleted {

	// TODO
	@OneToOne
	@JoinColumn(name = "fallId")
	private Fall fall;

	@OneToOne
	@JoinColumn(name = "mandantId")
	private Kontakt mandant;

	@Column(length = 25)
	private String rechnungsId;

	@Converter(name = "ElexisDBStringDateConverter", converterClass = ElexisDBStringDateConverter.class)
	@Convert("ElexisDBStringDateConverter")
	private LocalDate datum;

	@Column(length = 25, name = "diagnosen")
	private String diagnosenId;

	@Column(length = 25, name = "leistungen")
	private String leistungenId;

	@Basic(fetch = FetchType.LAZY)
	@Convert(value = "VersionedResourceConverter")
	private VersionedResource eintrag;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "BEHANDLUNG", insertable = false)
	private List<Verrechnet> verrechnet;

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

	public String getRechnungsId() {
		return rechnungsId;
	}

	public void setRechnungsId(String rechnungsId) {
		this.rechnungsId = rechnungsId;
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

	public String getDiagnosenId() {
		return diagnosenId;
	}

	public void setDiagnosenId(String diagnosenId) {
		this.diagnosenId = diagnosenId;
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

	public List<Verrechnet> getVerrechnet() {
		return verrechnet;
	}

	public void setVerrechnet(List<Verrechnet> verrechnet) {
		this.verrechnet = verrechnet;
	}
}
