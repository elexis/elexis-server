package info.elexis.server.core.connector.elexis.jpa.model.annotated.meta;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.3.2.v20111125-r10461", date="2016-01-28T08:02:46")
@StaticMetamodel(Verrechnet.class)
public class Verrechnet_ { 

    public static volatile SingularAttribute<Verrechnet, String> klasse;
    public static volatile SingularAttribute<Verrechnet, String> leistungenCode;
    public static volatile SingularAttribute<Verrechnet, Kontakt> userID;

}