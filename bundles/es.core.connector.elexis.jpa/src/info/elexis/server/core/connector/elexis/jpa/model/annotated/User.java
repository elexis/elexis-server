package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.annotations.Convert;

@Entity
@Table(name = "USER_")
@XmlRootElement(name = "user")
public class User extends AbstractDBObjectWithExtInfo {

	@OneToOne
	@JoinColumn(name = "KONTAKT_ID")
	protected Kontakt kontakt;

	@Column(length = 64, name = "HASHED_PASSWORD")
	protected String hashedPassword;

	@Column(length = 64)
	protected String salt;
	
	@Convert("booleanStringConverter")
	@Column(name = "is_active")
	protected boolean active;
	
	@Convert("booleanStringConverter")
	@Column(name = "is_administrator")
	protected boolean administrator;
	
	@Basic(fetch = FetchType.LAZY)
	@Lob()
	protected String keystore;

	public Kontakt getKontakt() {
		return kontakt;
	}

	public void setKontakt(Kontakt kontakt) {
		this.kontakt = kontakt;
	}

	public String getHashedPassword() {
		return hashedPassword;
	}

	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isAdministrator() {
		return administrator;
	}

	public void setAdministrator(boolean administrator) {
		this.administrator = administrator;
	}

	public String getKeystore() {
		return keystore;
	}

	public void setKeystore(String keystore) {
		this.keystore = keystore;
	}
}
