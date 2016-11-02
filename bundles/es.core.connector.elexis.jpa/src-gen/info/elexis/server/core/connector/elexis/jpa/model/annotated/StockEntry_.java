package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Stock;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.0.v20160725-rNA")
@StaticMetamodel(StockEntry.class)
public class StockEntry_ { 

    public static volatile SingularAttribute<StockEntry, String> articleType;
    public static volatile SingularAttribute<StockEntry, Kontakt> provider;
    public static volatile SingularAttribute<StockEntry, String> articleId;
    public static volatile SingularAttribute<StockEntry, Integer> currentStock;
    public static volatile SingularAttribute<StockEntry, Integer> minimumStock;
    public static volatile SingularAttribute<StockEntry, Integer> fractionUnits;
    public static volatile SingularAttribute<StockEntry, Stock> stock;
    public static volatile SingularAttribute<StockEntry, Integer> maximumStock;

}