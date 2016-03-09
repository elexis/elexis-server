package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import java.util.Map;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.2.v20151217-rNA", date="2016-03-08T11:46:56")
@StaticMetamodel(Verrechnet.class)
public class Verrechnet_ { 

    public static volatile SingularAttribute<Verrechnet, Integer> vk_preis;
    public static volatile SingularAttribute<Verrechnet, String> vk_scale;
    public static volatile SingularAttribute<Verrechnet, String> leistungenCode;
    public static volatile SingularAttribute<Verrechnet, Behandlung> behandlung;
    public static volatile SingularAttribute<Verrechnet, Integer> scale;
    public static volatile SingularAttribute<Verrechnet, String> leistungenText;
    public static volatile SingularAttribute<Verrechnet, Integer> ek_kosten;
    public static volatile SingularAttribute<Verrechnet, String> klasse;
    public static volatile SingularAttribute<Verrechnet, Integer> zahl;
    public static volatile SingularAttribute<Verrechnet, Integer> vk_tp;
    public static volatile SingularAttribute<Verrechnet, Integer> scale2;
    public static volatile SingularAttribute<Verrechnet, Map> detail;
    public static volatile SingularAttribute<Verrechnet, Kontakt> user;

}