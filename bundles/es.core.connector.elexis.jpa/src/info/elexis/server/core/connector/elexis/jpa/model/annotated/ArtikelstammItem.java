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
	
	@Column(length = 4)
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
	
	@Column
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
}

