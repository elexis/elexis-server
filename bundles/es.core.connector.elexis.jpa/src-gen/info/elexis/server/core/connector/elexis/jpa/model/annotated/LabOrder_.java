package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult;
import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.2.v20151217-rNA", date="2016-03-16T15:24:20")
@StaticMetamodel(LabOrder.class)
public class LabOrder_ { 

    public static volatile SingularAttribute<LabOrder, Kontakt> mandator;
    public static volatile SingularAttribute<LabOrder, LabResult> result;
    public static volatile SingularAttribute<LabOrder, LabItem> item;
    public static volatile SingularAttribute<LabOrder, String> orderid;
    public static volatile SingularAttribute<LabOrder, Kontakt> patient;
    public static volatile SingularAttribute<LabOrder, LocalDateTime> time;
    public static volatile SingularAttribute<LabOrder, LocalDateTime> observationTime;
    public static volatile SingularAttribute<LabOrder, String> state;
    public static volatile SingularAttribute<LabOrder, Kontakt> user;
    public static volatile SingularAttribute<LabOrder, String> groupname;

}