package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import ch.elexis.core.types.AddressType;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.0.v20160725-rNA")
@StaticMetamodel(ZusatzAdresse.class)
public class ZusatzAdresse_ { 

    public static volatile SingularAttribute<ZusatzAdresse, String> zip;
    public static volatile SingularAttribute<ZusatzAdresse, String> country;
    public static volatile SingularAttribute<ZusatzAdresse, String> city;
    public static volatile SingularAttribute<ZusatzAdresse, AddressType> addressType;
    public static volatile SingularAttribute<ZusatzAdresse, Kontakt> contact;
    public static volatile SingularAttribute<ZusatzAdresse, String> street1;
    public static volatile SingularAttribute<ZusatzAdresse, String> writtenAddress;
    public static volatile SingularAttribute<ZusatzAdresse, String> street2;

}