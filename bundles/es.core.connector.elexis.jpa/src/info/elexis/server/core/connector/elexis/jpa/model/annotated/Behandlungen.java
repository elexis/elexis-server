package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.time.LocalDate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.ReadTransformer;
import org.eclipse.persistence.annotations.WriteTransformer;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.transformer.ElexisDBStringDateTransformer;

@Entity
@Table(name = "behandlungen")
public class Behandlungen extends AbstractDBObject {

	// TODO
	@OneToOne
	@JoinColumn(name = "fallId")
	private Faelle fall;

	@OneToOne
	@JoinColumn(name = "mandantId")
	private Kontakt mandant;

	@Column(length = 25)
	private String rechnungsId;

	@ReadTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	@WriteTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	private LocalDate datum;

	@Column(length = 25, name = "diagnosen")
	private String diagnosenId;

	@Column(length = 25, name = "leistungen")
	private String leistungenId;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(length = 20000000)
	private byte[] eintrag;

	public Faelle getFall() {
		return fall;
	}

	public void setFall(Faelle fall) {
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

	public byte[] getEintrag() {
		return eintrag;
	}

	public void setEintrag(byte[] eintrag) {
		this.eintrag = eintrag;
	}
}
