package info.elexis.server.core.connector.elexis.jpa.model.annotated.meta;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Brief;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Heap;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.3.2.v20111125-r10461", date="2016-01-24T20:40:12")
@StaticMetamodel(Brief.class)
public class Brief_ { 

    public static volatile SingularAttribute<Brief, String> betreff;
    public static volatile SingularAttribute<Brief, Date> datum;
    public static volatile SingularAttribute<Brief, Date> modifiziert;
    public static volatile SingularAttribute<Brief, String> path;
    public static volatile SingularAttribute<Brief, Kontakt> absender;
    public static volatile SingularAttribute<Brief, Date> gedruckt;
    public static volatile SingularAttribute<Brief, Kontakt> patient;
    public static volatile SingularAttribute<Brief, String> typ;
    public static volatile SingularAttribute<Brief, String> mimetype;
    public static volatile SingularAttribute<Brief, Kontakt> empfaenger;
    public static volatile SingularAttribute<Brief, Heap> content;

}