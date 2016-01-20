package info.elexis.server.core.connector.elexis.jpa.model.annotated.meta;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.KontaktAdressJoint;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.3.2.v20111125-r10461", date="2016-01-20T16:24:51")
@StaticMetamodel(KontaktAdressJoint.class)
public class KontaktAdressJoint_ { 

    public static volatile SingularAttribute<KontaktAdressJoint, Integer> otherRType;
    public static volatile SingularAttribute<KontaktAdressJoint, Kontakt> otherKontakt;
    public static volatile SingularAttribute<KontaktAdressJoint, Kontakt> myKontakt;
    public static volatile SingularAttribute<KontaktAdressJoint, String> bezug;
    public static volatile SingularAttribute<KontaktAdressJoint, Integer> myRType;

}