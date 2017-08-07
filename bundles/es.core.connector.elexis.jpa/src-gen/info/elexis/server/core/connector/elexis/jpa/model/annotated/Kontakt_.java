package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import ch.elexis.core.types.Country;
import ch.elexis.core.types.Gender;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.KontaktAdressJoint;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Userconfig;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ZusatzAdresse;
import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.0.v20160725-rNA")
@StaticMetamodel(Kontakt.class)
public class Kontakt_ { 

    public static volatile SingularAttribute<Kontakt, String> allergies;
    public static volatile SingularAttribute<Kontakt, Country> country;
    public static volatile MapAttribute<Kontakt, String, ZusatzAdresse> addresses;
    public static volatile SingularAttribute<Kontakt, String> code;
    public static volatile SingularAttribute<Kontakt, Gender> gender;
    public static volatile SingularAttribute<Kontakt, String> city;
    public static volatile SingularAttribute<Kontakt, String> phone2;
    public static volatile SingularAttribute<Kontakt, Boolean> organisation;
    public static volatile SingularAttribute<Kontakt, String> titelSuffix;
    public static volatile SingularAttribute<Kontakt, byte[]> sysAnamnese;
    public static volatile SingularAttribute<Kontakt, String> description2;
    public static volatile SingularAttribute<Kontakt, String> description3;
    public static volatile SingularAttribute<Kontakt, String> description1;
    public static volatile SingularAttribute<Kontakt, String> phone1;
    public static volatile ListAttribute<Kontakt, Fall> faelle;
    public static volatile SingularAttribute<Kontakt, String> personalAnamnese;
    public static volatile SingularAttribute<Kontakt, String> titel;
    public static volatile SingularAttribute<Kontakt, Boolean> patient;
    public static volatile SingularAttribute<Kontakt, String> street;
    public static volatile SingularAttribute<Kontakt, String> fax;
    public static volatile SingularAttribute<Kontakt, String> email;
    public static volatile SingularAttribute<Kontakt, Boolean> mandator;
    public static volatile SingularAttribute<Kontakt, String> zip;
    public static volatile SingularAttribute<Kontakt, String> website;
    public static volatile ListAttribute<Kontakt, Userconfig> userconfig;
    public static volatile SingularAttribute<Kontakt, String> diagnosen;
    public static volatile SingularAttribute<Kontakt, String> mobile;
    public static volatile SingularAttribute<Kontakt, String> anschrift;
    public static volatile CollectionAttribute<Kontakt, KontaktAdressJoint> relatedByContacts;
    public static volatile MapAttribute<Kontakt, String, KontaktAdressJoint> relatedContacts;
    public static volatile SingularAttribute<Kontakt, Boolean> laboratory;
    public static volatile SingularAttribute<Kontakt, LocalDate> dob;
    public static volatile SingularAttribute<Kontakt, Boolean> person;
    public static volatile SingularAttribute<Kontakt, String> comment;
    public static volatile SingularAttribute<Kontakt, String> familyAnamnese;
    public static volatile SingularAttribute<Kontakt, String> risk;
    public static volatile SingularAttribute<Kontakt, String> gruppe;
    public static volatile SingularAttribute<Kontakt, Boolean> user;

}