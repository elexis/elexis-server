package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Convert;

@Entity
@Table(name = "laboritems")
public class LabItem extends AbstractDBObjectIdDeleted  {

	@Column(name = "kuerzel", length = 80)
	private String code;
	
	@Column(name = "titel", length = 80)
	private String title;
	
	@OneToOne
	@JoinColumn(name = "laborID")
	private Kontakt labor;
	
	@Column(name = "RefMann", length=256)
	private String referenceMale;
	
	@Column(name = "RefFrauOrTx", length=256)
	private String referenceFemaleOrTx;
	
	@Column(name = "einheit", length = 20)
	private String unit;
	
	@Column(name = "typ", length = 1)
	private String type;
	
	@Column(name = "Gruppe", length = 25)
	private String group;
	
	@Convert(value = "IntegerStringConverter")
	private int priority;
	
	@Column(length = 128)
	private String billingCode;
	
	@Column(length = 100)
	private String export;
	
	@Column(length = 128)
	private String loinccode;
	
	@Column(length = 1)
	private String visible;
	
	@Column(length = 16)
	private String digits;
	
	@Column(length = 255)
	private String formula;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Kontakt getLabor() {
		return labor;
	}

	public void setLabor(Kontakt labor) {
		this.labor = labor;
	}

	public String getReferenceMale() {
		return referenceMale;
	}

	public void setReferenceMale(String referenceMale) {
		this.referenceMale = referenceMale;
	}

	public String getReferenceFemaleOrTx() {
		return referenceFemaleOrTx;
	}

	public void setReferenceFemaleOrTx(String referenceFemaleOrTx) {
		this.referenceFemaleOrTx = referenceFemaleOrTx;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getBillingCode() {
		return billingCode;
	}

	public void setBillingCode(String billingCode) {
		this.billingCode = billingCode;
	}

	public String getExport() {
		return export;
	}

	public void setExport(String export) {
		this.export = export;
	}

	public String getLoinccode() {
		return loinccode;
	}

	public void setLoinccode(String loinccode) {
		this.loinccode = loinccode;
	}

	public String getVisible() {
		return visible;
	}

	public void setVisible(String visible) {
		this.visible = visible;
	}

	public String getDigits() {
		return digits;
	}

	public void setDigits(String digits) {
		this.digits = digits;
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}
}
