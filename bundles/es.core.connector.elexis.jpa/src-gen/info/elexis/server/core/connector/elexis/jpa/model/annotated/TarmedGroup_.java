package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedExtension;
import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.0.v20160725-rNA")
@StaticMetamodel(TarmedGroup.class)
public class TarmedGroup_ { 

    public static volatile SingularAttribute<TarmedGroup, String> groupName;
    public static volatile SingularAttribute<TarmedGroup, TarmedExtension> extension;
    public static volatile SingularAttribute<TarmedGroup, String> law;
    public static volatile SingularAttribute<TarmedGroup, LocalDate> validFrom;
    public static volatile SingularAttribute<TarmedGroup, String> rawServices;
    public static volatile SingularAttribute<TarmedGroup, LocalDate> validTo;

}