package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabItem;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.2.v20151217-rNA", date="2016-03-21T11:29:42")
@StaticMetamodel(LabResult.class)
public class LabResult_ { 

    public static volatile SingularAttribute<LabResult, LocalDate> date;
    public static volatile SingularAttribute<LabResult, String> refMale;
    public static volatile SingularAttribute<LabResult, LabItem> item;
    public static volatile SingularAttribute<LabResult, LocalDateTime> transmissiontime;
    public static volatile SingularAttribute<LabResult, String> origin;
    public static volatile SingularAttribute<LabResult, Integer> flags;
    public static volatile SingularAttribute<LabResult, String> zeit;
    public static volatile SingularAttribute<LabResult, String> result;
    public static volatile SingularAttribute<LabResult, String> unit;
    public static volatile SingularAttribute<LabResult, String> originId;
    public static volatile SingularAttribute<LabResult, Kontakt> patient;
    public static volatile SingularAttribute<LabResult, LocalDateTime> analysetime;
    public static volatile SingularAttribute<LabResult, LocalDateTime> observationtime;
    public static volatile SingularAttribute<LabResult, String> comment;
    public static volatile SingularAttribute<LabResult, String> refFemale;

}