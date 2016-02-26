package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabItem;
import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.2.v20151217-rNA", date="2016-02-26T15:31:36")
@StaticMetamodel(LabResult.class)
public class LabResult_ { 

    public static volatile SingularAttribute<LabResult, LocalDate> datum;
    public static volatile SingularAttribute<LabResult, String> refMale;
    public static volatile SingularAttribute<LabResult, LabItem> item;
    public static volatile SingularAttribute<LabResult, String> transmissiontime;
    public static volatile SingularAttribute<LabResult, String> origin;
    public static volatile SingularAttribute<LabResult, String> flags;
    public static volatile SingularAttribute<LabResult, String> resultat;
    public static volatile SingularAttribute<LabResult, String> zeit;
    public static volatile SingularAttribute<LabResult, String> unit;
    public static volatile SingularAttribute<LabResult, String> originId;
    public static volatile SingularAttribute<LabResult, Kontakt> patient;
    public static volatile SingularAttribute<LabResult, String> analysetime;
    public static volatile SingularAttribute<LabResult, String> observationtime;
    public static volatile SingularAttribute<LabResult, String> comment;
    public static volatile SingularAttribute<LabResult, String> refFemale;

}