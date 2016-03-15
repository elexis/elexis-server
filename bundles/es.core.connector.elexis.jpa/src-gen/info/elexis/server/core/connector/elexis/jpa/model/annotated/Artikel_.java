package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.2.v20151217-rNA", date="2016-03-15T09:14:29")
@StaticMetamodel(Artikel.class)
public class Artikel_ { 

    public static volatile SingularAttribute<Artikel, String> istbestand;
    public static volatile SingularAttribute<Artikel, String> maxbestand;
    public static volatile SingularAttribute<Artikel, String> vkPreis;
    public static volatile SingularAttribute<Artikel, String> Typ;
    public static volatile SingularAttribute<Artikel, LocalDate> validFrom;
    public static volatile SingularAttribute<Artikel, String> codeclass;
    public static volatile SingularAttribute<Artikel, String> ekPreis;
    public static volatile SingularAttribute<Artikel, String> subId;
    public static volatile SingularAttribute<Artikel, String> lastImport;
    public static volatile SingularAttribute<Artikel, String> ean;
    public static volatile SingularAttribute<Artikel, String> klasse;
    public static volatile SingularAttribute<Artikel, String> nameIntern;
    public static volatile SingularAttribute<Artikel, String> name;
    public static volatile SingularAttribute<Artikel, String> extId;
    public static volatile SingularAttribute<Artikel, String> atcCode;
    public static volatile SingularAttribute<Artikel, Kontakt> lieferant;
    public static volatile SingularAttribute<Artikel, String> minbestand;
    public static volatile SingularAttribute<Artikel, LocalDate> validTo;

}