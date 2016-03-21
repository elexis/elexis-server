package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.2.v20151217-rNA", date="2016-03-21T11:29:52")
@StaticMetamodel(PhysioLeistung.class)
public class PhysioLeistung_ { 

    public static volatile SingularAttribute<PhysioLeistung, String> titel;
    public static volatile SingularAttribute<PhysioLeistung, LocalDate> validUntil;
    public static volatile SingularAttribute<PhysioLeistung, String> description;
    public static volatile SingularAttribute<PhysioLeistung, String> ziffer;
    public static volatile SingularAttribute<PhysioLeistung, LocalDate> validFrom;
    public static volatile SingularAttribute<PhysioLeistung, String> tp;

}