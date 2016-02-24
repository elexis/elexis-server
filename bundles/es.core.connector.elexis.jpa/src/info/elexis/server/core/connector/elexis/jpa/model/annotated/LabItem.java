package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "laboritems")
public class LabItem extends AbstractDBObjectIdDeleted  {

}
