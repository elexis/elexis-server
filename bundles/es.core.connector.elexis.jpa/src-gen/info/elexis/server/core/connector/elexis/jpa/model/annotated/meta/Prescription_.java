package info.elexis.server.core.connector.elexis.jpa.model.annotated.meta;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObject;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription;
import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.3.2.v20111125-r10461", date="2016-01-28T08:13:43")
@StaticMetamodel(Prescription.class)
public class Prescription_ { 

    public static volatile SingularAttribute<Prescription, String> artikelID;
    public static volatile SingularAttribute<Prescription, String> rezeptID;
    public static volatile SingularAttribute<Prescription, AbstractDBObject> artikel;
    public static volatile SingularAttribute<Prescription, String> anzahl;
    public static volatile SingularAttribute<Prescription, Kontakt> patientID;
    public static volatile SingularAttribute<Prescription, String> dosis;
    public static volatile SingularAttribute<Prescription, String> bemerkung;
    public static volatile SingularAttribute<Prescription, LocalDate> dateUntil;
    public static volatile SingularAttribute<Prescription, LocalDate> dateFrom;

}