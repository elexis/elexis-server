package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "LEISTUNGEN")
public class Verrechnet extends AbstractDBObject {

	@OneToOne
	@JoinColumn(name = "userID")
	private Kontakt userID;
	
}
