package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.0.v20160725-rNA")
@StaticMetamodel(TarmedKumulation.class)
public class TarmedKumulation_ { 

    public static volatile SingularAttribute<TarmedKumulation, String> slaveCode;
    public static volatile SingularAttribute<TarmedKumulation, String> validSide;
    public static volatile SingularAttribute<TarmedKumulation, String> masterCode;
    public static volatile SingularAttribute<TarmedKumulation, String> view;
    public static volatile SingularAttribute<TarmedKumulation, String> slaveArt;
    public static volatile SingularAttribute<TarmedKumulation, String> masterArt;
    public static volatile SingularAttribute<TarmedKumulation, String> typ;
    public static volatile SingularAttribute<TarmedKumulation, LocalDate> validFrom;
    public static volatile SingularAttribute<TarmedKumulation, LocalDate> validTo;

}