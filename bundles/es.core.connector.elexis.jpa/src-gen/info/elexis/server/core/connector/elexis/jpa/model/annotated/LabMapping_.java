package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabItem;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.0.v20160725-rNA")
@StaticMetamodel(LabMapping.class)
public class LabMapping_ { 

    public static volatile SingularAttribute<LabMapping, Boolean> charge;
    public static volatile SingularAttribute<LabMapping, String> itemname;
    public static volatile SingularAttribute<LabMapping, Kontakt> origin;
    public static volatile SingularAttribute<LabMapping, LabItem> labItem;

}