package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.0.v20160725-rNA")
@StaticMetamodel(DbImage.class)
public class DbImage_ { 

    public static volatile SingularAttribute<DbImage, LocalDate> date;
    public static volatile SingularAttribute<DbImage, byte[]> image;
    public static volatile SingularAttribute<DbImage, String> prefix;
    public static volatile SingularAttribute<DbImage, String> title;

}