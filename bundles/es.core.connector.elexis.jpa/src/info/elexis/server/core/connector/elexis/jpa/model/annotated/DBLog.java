package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.converter.ElexisDBStringDateConverter;

@Entity
@Table(name = "LOGS")
public class DBLog extends AbstractDBObjectIdDeletedExtInfo {

	@Column(length = 255)
	protected String oid;
	
	@Converter(name = "ElexisDBStringDateConverter", converterClass = ElexisDBStringDateConverter.class)
	@Convert("ElexisDBStringDateConverter")
	protected LocalDate datum;

	@Column(length = 20)
	protected String typ;
	
	@Column(length = 25)
	protected String userId;
	
	@Column(length = 255)
	protected String station;
}
