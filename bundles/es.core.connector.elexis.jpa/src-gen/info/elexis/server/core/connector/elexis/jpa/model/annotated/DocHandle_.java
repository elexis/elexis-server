package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import java.time.LocalDate;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.2.v20151217-rNA", date="2016-02-10T10:53:57")
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