package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.0.v20160725-rNA")
@StaticMetamodel(DocHandle.class)
public class DocHandle_ { 

    public static volatile SingularAttribute<DocHandle, LocalDate> datum;
    public static volatile SingularAttribute<DocHandle, String> path;
    public static volatile SingularAttribute<DocHandle, String> keywords;
    public static volatile SingularAttribute<DocHandle, Kontakt> kontakt;
    public static volatile SingularAttribute<DocHandle, byte[]> doc;
    public static volatile SingularAttribute<DocHandle, String> mimetype;
    public static volatile SingularAttribute<DocHandle, LocalDate> creationDate;
    public static volatile SingularAttribute<DocHandle, String> category;
    public static volatile SingularAttribute<DocHandle, String> title;

}