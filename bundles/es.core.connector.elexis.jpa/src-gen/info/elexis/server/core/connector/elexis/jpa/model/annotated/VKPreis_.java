package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.2.v20151217-rNA", date="2016-03-15T09:14:29")
@StaticMetamodel(VKPreis.class)
public class VKPreis_ { 

    public static volatile SingularAttribute<VKPreis, LocalDate> datum_von;
    public static volatile SingularAttribute<VKPreis, String> typ;
    public static volatile SingularAttribute<VKPreis, String> id;
    public static volatile SingularAttribute<VKPreis, String> multiplikator;
    public static volatile SingularAttribute<VKPreis, LocalDate> datum_bis;

}