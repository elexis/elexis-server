package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "EIGENLEISTUNGEN")
public class Eigenleistung extends AbstractDBObjectIdDeleted {

	@Column(length = 20)
	private String code;

	@Column(length = 80)
	private String description;

	@Column(length = 6)
	private String basePrice;

	@Column(length = 6)
	private String salePrice;

	@Column(length = 4)
	private String time;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(String basePrice) {
		this.basePrice = basePrice;
	}

	public String getSalePrice() {
		return salePrice;
	}

	public void setSalePrice(String salePrice) {
		this.salePrice = salePrice;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
}
