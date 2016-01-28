package info.elexis.server.core.connector.elexis.jpa.model.annotated.meta;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.DocHandle;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import java.time.LocalDate;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.3.2.v20111125-r10461", date="2016-01-28T08:02:46")
@StaticMetamodel(DocHandle.class)
public class DocHandle_ { 

    public static volatile SingularAttribute<DocHandle, LocalDate> datum;
    public static volatile SingularAttribute<DocHandle, String> path;
    public static volatile SingularAttribute<DocHandle, String> keywords;
    public static volatile SingularAttribute<DocHandle, Kontakt> kontakt;
    public static volatile SingularAttribute<DocHandle, byte[]> doc;
    public static volatile SingularAttribute<DocHandle, String> mimetype;
    public static volatile SingularAttribute<DocHandle, String> category;
    public static volatile SingularAttribute<DocHandle, String> title;
    public static volatile SingularAttribute<DocHandle, Date> creationDate;

}