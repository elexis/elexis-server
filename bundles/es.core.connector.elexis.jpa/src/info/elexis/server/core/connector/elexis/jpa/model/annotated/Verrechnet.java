package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "LEISTUNGEN")
public class Verrechnet extends AbstractDBObjectIdDeleted {

	@OneToOne
	@JoinColumn(name = "userID")
	private Kontakt userID;

	@Column(length = 80)
	private String klasse;

	@Column(length = 25, name = "leistg_code")
	private String leistungenCode;

	public Kontakt getUserID() {
		return userID;
	}

	public void setUserID(Kontakt userID) {
		this.userID = userID;
	}

	public String getKlasse() {
		return klasse;
	}

	public void setKlasse(String klasse) {
		this.klasse = klasse;
	}
	
	public String getLeistungenCode() {
		return leistungenCode;
	}
	
	public void setLeistungenCode(String leistungenCode) {
		this.leistungenCode = leistungenCode;
	}
}
