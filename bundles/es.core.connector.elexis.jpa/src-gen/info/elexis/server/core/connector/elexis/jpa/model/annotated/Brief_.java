package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.0.v20160725-rNA")
@StaticMetamodel(Brief.class)
public class Brief_ { 

    public static volatile SingularAttribute<Brief, String> note;
    public static volatile SingularAttribute<Brief, String> subject;
    public static volatile SingularAttribute<Brief, String> typ;
    public static volatile SingularAttribute<Brief, LocalDateTime> creationDate;
    public static volatile SingularAttribute<Brief, Boolean> geloescht;
    public static volatile SingularAttribute<Brief, String> path;
    public static volatile SingularAttribute<Brief, LocalDate> gedruckt;
    public static volatile SingularAttribute<Brief, Kontakt> sender;
    public static volatile SingularAttribute<Brief, Kontakt> patient;
    public static volatile SingularAttribute<Brief, LocalDateTime> modifiedDate;
    public static volatile SingularAttribute<Brief, Kontakt> recipient;
    public static volatile SingularAttribute<Brief, String> mimetype;
    public static volatile SingularAttribute<Brief, Behandlung> consultation;

}