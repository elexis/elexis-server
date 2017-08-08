package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.converter.ElexisDBStringDateConverter;

@Entity
@Table(name = "TARMED")
public class TarmedLeistung extends AbstractDBObjectIdDeleted {

	@Column(length = 14)
	private String parent;

	@Column(length = 5)
	private String digniQuanti;

	@Column(length = 4)
	private String digniQuali;

	@Column(length = 4)
	private String sparte;

	@Converter(name = "ElexisDBStringDateConverter", converterClass = ElexisDBStringDateConverter.class)
	@Convert("ElexisDBStringDateConverter")
	private LocalDate gueltigVon;

	@Converter(name = "ElexisDBStringDateConverter", converterClass = ElexisDBStringDateConverter.class)
	@Convert("ElexisDBStringDateConverter")
	private LocalDate gueltigBis;

	@Column(length = 25)
	private String nickname;

	@Column(length = 255)
	private String tx255;

	@Column(length = 25)
	private String code;

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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public TarmedExtension getExtension() {
		return extension;
	}

	public void setExtension(TarmedExtension extension) {
		this.extension = extension;
	}

}
