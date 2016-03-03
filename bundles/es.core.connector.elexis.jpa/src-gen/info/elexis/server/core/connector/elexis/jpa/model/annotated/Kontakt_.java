package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Userconfig;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Xid;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.types.Country;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.types.Gender;
import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.2.v20151217-rNA", date="2016-03-03T12:13:10")
@StaticMetamodel(Kontakt.class)
public class Kontakt_ { 

    public static volatile SingularAttribute<Kontakt, String> persAnamnese;
    public static volatile SingularAttribute<Kontakt, String> allergien;
    public static volatile SingularAttribute<Kontakt, Gender> geschlecht;
    public static volatile SingularAttribute<Kontakt, String> titelSuffix;
    public static volatile SingularAttribute<Kontakt, byte[]> sysAnamnese;
    public static volatile SingularAttribute<Kontakt, String> bezeichnung1;
    public static volatile SingularAttribute<Kontakt, String> bezeichnung2;
    public static volatile SingularAttribute<Kontakt, String> bezeichnung3;
    public static volatile ListAttribute<Kontakt, Fall> faelle;
    public static volatile SingularAttribute<Kontakt, Boolean> istLabor;
    public static volatile SingularAttribute<Kontakt, LocalDate> geburtsdatum;
    public static volatile SingularAttribute<Kontakt, String> titel;
    public static volatile SingularAttribute<Kontakt, Boolean> istOrganisation;
    public static volatile SingularAttribute<Kontakt, Country> land;
    public static volatile SingularAttribute<Kontakt, Boolean> istMandant;
    public static volatile SingularAttribute<Kontakt, String> fax;
    public static volatile SingularAttribute<Kontakt, String> email;
    public static volatile SingularAttribute<Kontakt, String> risiken;
    public static volatile SingularAttribute<Kontakt, String> telefon1;
    public static volatile SingularAttribute<Kontakt, String> telefon2;
    public static volatile SingularAttribute<Kontakt, String> website;
    public static volatile SingularAttribute<Kontakt, String> strasse;
    public static volatile ListAttribute<Kontakt, Userconfig> userconfig;
    public static volatile SingularAttribute<Kontakt, String> diagnosen;
    public static volatile SingularAttribute<Kontakt, Boolean> istPatient;
    public static volatile SingularAttribute<Kontakt, String> natelNr;
    public static volatile SingularAttribute<Kontakt, String> anschrift;
    public static volatile SingularAttribute<Kontakt, Boolean> istPerson;
    public static volatile SingularAttribute<Kontakt, String> patientNr;
    public static volatile SingularAttribute<Kontakt, String> ort;
    public static volatile SingularAttribute<Kontakt, Boolean> istAnwender;
    public static volatile SingularAttribute<Kontakt, String> bemerkung;
    public static volatile MapAttribute<Kontakt, String, Xid> xids;
    public static volatile SingularAttribute<Kontakt, String> gruppe;
    public static volatile SingularAttribute<Kontakt, String> famAnamnese;
    public static volatile SingularAttribute<Kontakt, String> plz;

}