package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Heap;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.2.v20151217-rNA", date="2016-02-26T15:31:36")
@StaticMetamodel(Brief.class)
public class Brief_ { 

    public static volatile SingularAttribute<Brief, String> betreff;
    public static volatile SingularAttribute<Brief, LocalDate> datum;
    public static volatile SingularAttribute<Brief, LocalDate> modifiziert;
    public static volatile SingularAttribute<Brief, String> path;
    public static volatile SingularAttribute<Brief, Kontakt> absender;
    public static volatile SingularAttribute<Brief, LocalDate> gedruckt;
    public static volatile SingularAttribute<Brief, Kontakt> patient;
    public static volatile SingularAttribute<Brief, String> typ;
    public static volatile SingularAttribute<Brief, String> mimetype;
    public static volatile SingularAttribute<Brief, Kontakt> empfaenger;
    public static volatile SingularAttribute<Brief, Heap> content;

}