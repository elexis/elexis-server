package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.2.v20151217-rNA", date="2016-03-10T09:53:01")
@StaticMetamodel(Fall.class)
public class Fall_ { 

    public static volatile SingularAttribute<Fall, LocalDate> datumVon;
    public static volatile SingularAttribute<Fall, String> fallNummer;
    public static volatile SingularAttribute<Fall, String> grund;
    public static volatile SingularAttribute<Fall, String> bezeichnung;
    public static volatile SingularAttribute<Fall, String> diagnosen;
    public static volatile ListAttribute<Fall, Behandlung> consultations;
    public static volatile SingularAttribute<Fall, String> betriebsNummer;
    public static volatile SingularAttribute<Fall, String> versNummer;
    public static volatile SingularAttribute<Fall, Kontakt> garantKontakt;
    public static volatile SingularAttribute<Fall, Kontakt> kostentrKontakt;
    public static volatile SingularAttribute<Fall, LocalDate> datumBis;
    public static volatile SingularAttribute<Fall, String> gesetz;
    public static volatile SingularAttribute<Fall, Kontakt> patientKontakt;
    public static volatile SingularAttribute<Fall, String> status;

}