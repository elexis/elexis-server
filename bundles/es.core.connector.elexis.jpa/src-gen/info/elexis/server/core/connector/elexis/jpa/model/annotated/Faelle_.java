package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.2.v20151217-rNA", date="2016-02-10T10:53:57")
@StaticMetamodel(Faelle.class)
public class Faelle_ { 

    public static volatile SingularAttribute<Faelle, LocalDate> datumVon;
    public static volatile SingularAttribute<Faelle, String> fallNummer;
    public static volatile SingularAttribute<Faelle, String> grund;
    public static volatile SingularAttribute<Faelle, String> bezeichnung;
    public static volatile SingularAttribute<Faelle, String> diagnosen;
    public static volatile SingularAttribute<Faelle, String> betriebsNummer;
    public static volatile SingularAttribute<Faelle, String> versNummer;
    public static volatile SingularAttribute<Faelle, byte[]> extInfo;
    public static volatile SingularAttribute<Faelle, Kontakt> garantKontakt;
    public static volatile SingularAttribute<Faelle, Kontakt> kostentrKontakt;
    public static volatile SingularAttribute<Faelle, LocalDate> datumBis;
    public static volatile SingularAttribute<Faelle, String> gesetz;
    public static volatile SingularAttribute<Faelle, Kontakt> patientKontakt;
    public static volatile SingularAttribute<Faelle, String> status;

}