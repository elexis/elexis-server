package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.0.v20160725-rNA")
@StaticMetamodel(VKPreis.class)
public class VKPreis_ { 

    public static volatile SingularAttribute<VKPreis, LocalDate> datum_von;
    public static volatile SingularAttribute<VKPreis, String> typ;
    public static volatile SingularAttribute<VKPreis, String> id;
    public static volatile SingularAttribute<VKPreis, String> multiplikator;
    public static volatile SingularAttribute<VKPreis, LocalDate> datum_bis;

}