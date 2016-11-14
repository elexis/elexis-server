package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.StockEntry;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.0.v20160725-rNA")
@StaticMetamodel(Stock.class)
public class Stock_ { 

    public static volatile SingularAttribute<Stock, Kontakt> owner;
    public static volatile SingularAttribute<Stock, String> driverConfig;
    public static volatile ListAttribute<Stock, StockEntry> entries;
    public static volatile SingularAttribute<Stock, String> code;
    public static volatile SingularAttribute<Stock, Kontakt> responsible;
    public static volatile SingularAttribute<Stock, String> description;
    public static volatile SingularAttribute<Stock, String> location;
    public static volatile SingularAttribute<Stock, Integer> priority;
    public static volatile SingularAttribute<Stock, String> driverUuid;

}