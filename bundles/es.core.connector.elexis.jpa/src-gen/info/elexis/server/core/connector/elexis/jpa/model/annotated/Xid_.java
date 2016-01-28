package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.types.XidQuality;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.2.v20151217-rNA", date="2016-01-28T15:19:50")
@StaticMetamodel(Xid.class)
public class Xid_ { 

    public static volatile SingularAttribute<Xid, String> domain;
    public static volatile SingularAttribute<Xid, String> type;
    public static volatile SingularAttribute<Xid, String> domainId;
    public static volatile SingularAttribute<Xid, String> object;
    public static volatile SingularAttribute<Xid, XidQuality> quality;

}