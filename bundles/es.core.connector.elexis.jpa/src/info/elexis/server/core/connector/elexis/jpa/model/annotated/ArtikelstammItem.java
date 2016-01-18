package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "artikelstamm_ch")
public class ArtikelstammItem extends AbstractDBObjectWithExtInfo {
	@Column(length = 1)
	private String type;
	
	@Column(length = 1)
	private String bb;
	
	@Column(name = "CUMM_VERSION", length = 4)
	private String cummVersion;
	
	@Column(length = 14)
	private String gtin;
	
	@Column(length = 7)
	private String phar;
	
	@Column(length = 50)
	private String dscr;
	
	@Column(length = 50)
	private String adddscr;
	
	@Column(length = 10)
	private String atc;
	
	@Column(length = 13)
	private String comp_gln;
	
	@Column(length = 255)
	private String comp_name;
	
	@Column(length = 10)
	private String pexf;
	
	@Column(length = 10)
	private String ppub;

	@Column(length = 6)
	private String pkg_size;
	
	@Column(length = 1)
	private String sl_entry;
	
	@Column(length = 1)
	private String ikscat;

	@Column(length = 1)
	private String limitation;
	
	// spell error in creating table :(
	@Column(length = 4, name="limitiation_pts")
	private String limitation_pts;
	
	@Column
	@Lob
	private String limitation_txt;
	
	@Column(length = 1)
	private String generic_type;
	
	@Column(length = 1)
	private String has_generic;
	
	@Column(length = 1)
	private String lppv;
	
	@Column(length = 6)
	private String deductible;
	
	@Column(length = 1)
	private String narcotic;
	
	@Column(length = 20)
	private String narcotic_cas;
	
	@Column(length = 1)
	private String vaccine;
	
	@JoinColumn(name = "lieferantId")
	private Kontakt lieferant;
	
	@Column(length = 4)
	private String maxbestand;
	
	@Column(length = 4)
	private String minbestand;
	
	@Column(length = 4)
	private String istbestand;
	
	@Column(length = 4)
	private String verkaufseinheit;
	
	@Column(length = 4)
	private String anbruch;
	
	@Column(length = 10)
	private String prodno;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBb() {
		return bb;
	}

	public void setBb(String bb) {
		this.bb = bb;
	}

	public String getCummVersion() {
		return cummVersion;
	}

	public void setCummVersion(String cummVersion) {
		this.cummVersion = cummVersion;
	}

	public String getGtin() {
		return gtin;
	}

	public void setGtin(String gtin) {
		this.gtin = gtin;
	}

	public String getPhar() {
		return phar;
	}

	public void setPhar(String phar) {
		this.phar = phar;
	}

	public String getDscr() {
		return dscr;
	}

	public void setDscr(String dscr) {
		this.dscr = dscr;
	}

	public String getAdddscr() {
		return adddscr;
	}

	public void setAdddscr(String adddscr) {
		this.adddscr = adddscr;
	}

	public String getAtc() {
		return atc;
	}

	public void setAtc(String atc) {
		this.atc = atc;
	}

	public String getComp_gln() {
		return comp_gln;
	}

	public void setComp_gln(String comp_gln) {
		this.comp_gln = comp_gln;
	}

	public String getComp_name() {
		return comp_name;
	}

	public void setComp_name(String comp_name) {
		this.comp_name = comp_name;
	}

	public String getPexf() {
		return pexf;
	}

	public void setPexf(String pexf) {
		this.pexf = pexf;
	}

	public String getPpub() {
		return ppub;
	}

	public void setPpub(String ppub) {
		this.ppub = ppub;
	}

	public String getPkg_size() {
		return pkg_size;
	}

	public void setPkg_size(String pkg_size) {
		this.pkg_size = pkg_size;
	}

	public String getSl_entry() {
		return sl_entry;
	}

	public void setSl_entry(String sl_entry) {
		this.sl_entry = sl_entry;
	}

	public String getIkscat() {
		return ikscat;
	}

	public void setIkscat(String ikscat) {
		this.ikscat = ikscat;
	}

	public String getLimitation() {
		return limitation;
	}

	public void setLimitation(String limitation) {
		this.limitation = limitation;
	}

	public String getLimitation_pts() {
		return limitation_pts;
	}

	public void setLimitation_pts(String limitation_pts) {
		this.limitation_pts = limitation_pts;
	}

	public String getLimitation_txt() {
		return limitation_txt;
	}

	public void setLimitation_txt(String limitation_txt) {
		this.limitation_txt = limitation_txt;
	}

	public String getGeneric_type() {
		return generic_type;
	}

	public void setGeneric_type(String generic_type) {
		this.generic_type = generic_type;
	}

	public String getHas_generic() {
		return has_generic;
	}

	public void setHas_generic(String has_generic) {
		this.has_generic = has_generic;
	}

	public String getLppv() {
		return lppv;
	}

	public void setLppv(String lppv) {
		this.lppv = lppv;
	}

	public String getDeductible() {
		return deductible;
	}

	public void setDeductible(String deductible) {
		this.deductible = deductible;
	}

	public String getNarcotic() {
		return narcotic;
	}

	public void setNarcotic(String narcotic) {
		this.narcotic = narcotic;
	}

	public String getNarcotic_cas() {
		return narcotic_cas;
	}

	public void setNarcotic_cas(String narcotic_cas) {
		this.narcotic_cas = narcotic_cas;
	}

	public String getVaccine() {
		return vaccine;
	}

	public void setVaccine(String vaccine) {
		this.vaccine = vaccine;
	}

	public Kontakt getLieferant() {
		return lieferant;
	}

	public void setLieferant(Kontakt lieferant) {
		this.lieferant = lieferant;
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

	public String getVerkaufseinheit() {
		return verkaufseinheit;
	}

	public void setVerkaufseinheit(String verkaufseinheit) {
		this.verkaufseinheit = verkaufseinheit;
	}

	public String getAnbruch() {
		return anbruch;
	}

	public void setAnbruch(String anbruch) {
		this.anbruch = anbruch;
	}

	public String getProdno() {
		return prodno;
	}

	public void setProdno(String prodno) {
		this.prodno = prodno;
	}
}

