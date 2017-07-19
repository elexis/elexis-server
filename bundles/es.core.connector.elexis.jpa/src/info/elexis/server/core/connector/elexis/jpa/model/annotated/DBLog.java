package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "LOGS")
public class DBLog extends AbstractDBObjectIdDeletedExtInfo {

	@Column(length = 255)
	protected String oid;
	
	@Column(length = 8)
	protected LocalDate datum;

	@Column(length = 20)
	protected String typ;
	
	@Column(length = 25)
	protected String userId;
	
	@Column(length = 255)
	protected String station;

	@Override
	public String getLabel() {
		return "DB-Log";
	}
}
