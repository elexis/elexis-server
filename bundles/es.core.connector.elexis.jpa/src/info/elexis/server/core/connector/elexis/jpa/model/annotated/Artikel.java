package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;

import ch.rgw.tools.StringTool;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.converter.ElexisDBStringDateConverter;

@Entity
@Table(name = "artikel")
public class Artikel extends AbstractDBObjectIdDeletedExtInfo {

	public static final String TYP_EIGENARTIKEL = "Eigenartikel";
	public static final String TYP_MIGEL = "MiGeL";
	public static final String TYP_MEDICAL = "Medical";
	public static final String TYP_MEDIKAMENT = "Medikament";

	public static final String FLD_EXTINFO_PACKAGESIZE = "Verpackungseinheit";
	
	@Column(length = 15)
	private String ean;

	@Column(length = 20, name = "SubID")
	private String subId;

	@OneToOne
	@JoinColumn(name = "LieferantID")
	private Kontakt lieferant;

	@Column(length = 80)
	private String klasse;

	@Column(length = 127)
	private String name;

	@Column(length = 127, name = "Name_intern")
	private String nameIntern;

	@Column(length = 4)
	private String maxbestand;

	@Column(length = 4)
	private String minbestand;

	@Column(length = 4)
	private String istbestand;

	@Column(length = 8, name = "EK_Preis")
	private String ekPreis;

	@Column(length = 8, name = "VK_Preis")
	private String vkPreis;

	@Column(length = 15)
	private String Typ;

	@Column(length = 10)
	private String codeclass;

	@Column(length = 25)
	private String extId;

	@Column(length = 8)
	private String lastImport;

	@Converter(name = "ElexisDBStringDateConverter", converterClass = ElexisDBStringDateConverter.class)
	@Convert("ElexisDBStringDateConverter")
	private LocalDate validFrom;

	@Converter(name = "ElexisDBStringDateConverter", converterClass = ElexisDBStringDateConverter.class)
	@Convert("ElexisDBStringDateConverter")
	private LocalDate validTo;

	@Column(length = 255, name = "ATC_code")
	private String atcCode;

	@Override
	public String getLabel() {
		String ret = getNameIntern();
		if (StringTool.isNothing(ret)) {
			ret = getName();
		}
		return ret;
	}

	public String getEan() {
		return ean;
	}

	public void setEan(String ean) {
		this.ean = ean;
	}

	public String getSubId() {
		return subId;
	}

	public void setSubId(String subId) {
		this.subId = subId;
	}

	public Kontakt getLieferant() {
		return lieferant;
	}

	public void setLieferant(Kontakt lieferant) {
		this.lieferant = lieferant;
	}

	public String getKlasse() {
		return klasse;
	}

	public void setKlasse(String klasse) {
		this.klasse = klasse;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameIntern() {
		return nameIntern;
	}

	public void setNameIntern(String nameIntern) {
		this.nameIntern = nameIntern;
	}

	public String getMaxbestand() {
		return maxbestand;
	}

	public void setMaxbestand(String maxbestand) {
		this.maxbestand = maxbestand;
	}

	public String getMinbestand() {
		return minbestand;
	}

	public void setMinbestand(String minbestand) {
		this.minbestand = minbestand;
	}

	public String getIstbestand() {
		return istbestand;
	}

	public void setIstbestand(String istbestand) {
		this.istbestand = istbestand;
	}

	public String getEkPreis() {
		return ekPreis;
	}

	public void setEkPreis(String ekPreis) {
		this.ekPreis = ekPreis;
	}

	public String getVkPreis() {
		return vkPreis;
	}

	public void setVkPreis(String vkPreis) {
		this.vkPreis = vkPreis;
	}

	public String getTyp() {
		return Typ;
	}

	public void setTyp(String typ) {
		Typ = typ;
	}

	public String getCodeclass() {
		return codeclass;
	}

	public void setCodeclass(String codeclass) {
		this.codeclass = codeclass;
	}

	public String getExtId() {
		return extId;
	}

	public void setExtId(String extId) {
		this.extId = extId;
	}

	public String getLastImport() {
		return lastImport;
	}

	public void setLastImport(String lastImport) {
		this.lastImport = lastImport;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public void setValidTo(LocalDate validTo) {
		this.validTo = validTo;
	}

	public String getAtcCode() {
		return atcCode;
	}

	public void setAtcCode(String atcCode) {
		this.atcCode = atcCode;
	}
}
