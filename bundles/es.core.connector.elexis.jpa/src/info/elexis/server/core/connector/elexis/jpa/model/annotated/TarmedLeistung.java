package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import ch.elexis.core.model.ICodeElement;

@Entity
@Table(name = "TARMED")
public class TarmedLeistung extends AbstractDBObjectIdDeleted implements ICodeElement {

	public static final String CODESYSTEM_NAME = "Tarmed";

	@Column(length = 14)
	private String parent;

	@Column(length = 5)
	private String digniQuanti;

	@Column(length = 4)
	private String digniQuali;

	@Column(length = 4)
	private String sparte;

	@Column(length = 4)
	private LocalDate gueltigVon;

	@Column(length = 4)
	private LocalDate gueltigBis;

	@Column(length = 25)
	private String nickname;

	@Column(length = 255)
	private String tx255;

	@Column(length = 25, name = "code")
	private String code_;

	@OneToOne
	@JoinColumn(name = "id", insertable = false, updatable = false)
	private TarmedExtension extension;

	@Transient
	public int getAL() {
		if (extension != null) {
			Object object = extension.getLimits().get("TP_AL");
			if (object != null) {
				try {
					double val = Double.parseDouble((String) object);
					return (int) Math.round(val * 100);
				} catch (NumberFormatException nfe) {
					/* ignore */
				}
			}
		}
		return 0;
	}

	@Transient
	public int getTL() {
		if (extension != null) {
			Object object = extension.getLimits().get("TP_TL");
			if (object != null) {
				try {
					double val = Double.parseDouble((String) object);
					return (int) Math.round(val * 100);
				} catch (NumberFormatException nfe) {
					/* ignore */
				}
			}

		}
		return 0;
	}

	public boolean requiresSide() {
		if (extension != null) {
			Object object = extension.getLimits().get("SEITE");
			if (object != null && Integer.parseInt((String) object) == 1) {
				return true;
			}
		}
		return false;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getDigniQuanti() {
		return digniQuanti;
	}

	public void setDigniQuanti(String digniQuanti) {
		this.digniQuanti = digniQuanti;
	}

	public String getDigniQuali() {
		return digniQuali;
	}

	public void setDigniQuali(String digniQuali) {
		this.digniQuali = digniQuali;
	}

	public String getSparte() {
		return sparte;
	}

	public void setSparte(String sparte) {
		this.sparte = sparte;
	}

	public LocalDate getGueltigVon() {
		return gueltigVon;
	}

	public void setGueltigVon(LocalDate gueltigVon) {
		this.gueltigVon = gueltigVon;
	}

	public LocalDate getGueltigBis() {
		return gueltigBis;
	}

	public void setGueltigBis(LocalDate gueltigBis) {
		this.gueltigBis = gueltigBis;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getTx255() {
		return tx255;
	}

	public void setTx255(String tx255) {
		this.tx255 = tx255;
	}

	public void setCode_(String code_) {
		this.code_ = code_;
	}

	public String getCode_() {
		return code_;
	}
	
	public TarmedExtension getExtension() {
		return extension;
	}

	public void setExtension(TarmedExtension extension) {
		this.extension = extension;
	}
	
	public String getCode() {
		return (code_ != null) ? code_ : getId();
	}

	@Override
	public String getCodeSystemName() {
		return CODESYSTEM_NAME;
	}

	@Override
	public String getText() {
		return getTx255();
	}
}
