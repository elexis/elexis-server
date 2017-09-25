package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import ch.elexis.core.model.issue.Priority;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Type;
import ch.elexis.core.model.issue.Visibility;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.0.v20160725-rNA")
@StaticMetamodel(Reminder.class)
public class Reminder_ { 

    public static volatile SingularAttribute<Reminder, LocalDate> dateDue;
    public static volatile SingularAttribute<Reminder, Type> actionType;
    public static volatile SingularAttribute<Reminder, Kontakt> creator;
    public static volatile SingularAttribute<Reminder, Visibility> visibility;
    public static volatile SingularAttribute<Reminder, String> subject;
    public static volatile SetAttribute<Reminder, Kontakt> responsible;
    public static volatile SingularAttribute<Reminder, String> responsibleValue;
    public static volatile SingularAttribute<Reminder, Kontakt> kontakt;
    public static volatile SingularAttribute<Reminder, String> params;
    public static volatile SingularAttribute<Reminder, String> message;
    public static volatile SingularAttribute<Reminder, Priority> priority;
    public static volatile SingularAttribute<Reminder, ProcessStatus> status;

}