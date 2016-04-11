package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.2.v20151217-rNA", date="2016-04-11T18:20:29")
@StaticMetamodel(Prescription.class)
public class Prescription_ { 

    public static volatile SingularAttribute<Prescription, String> prescriptionType;
    public static volatile SingularAttribute<Prescription, String> rezeptID;
    public static volatile SingularAttribute<Prescription, AbstractDBObjectIdDeleted> artikel;
    public static volatile SingularAttribute<Prescription, String> anzahl;
    public static volatile SingularAttribute<Prescription, Kontakt> patient;
    public static volatile SingularAttribute<Prescription, String> dosis;
    public static volatile SingularAttribute<Prescription, String> bemerkung;
    public static volatile SingularAttribute<Prescription, LocalDateTime> dateUntil;
    public static volatile SingularAttribute<Prescription, LocalDateTime> dateFrom;

}