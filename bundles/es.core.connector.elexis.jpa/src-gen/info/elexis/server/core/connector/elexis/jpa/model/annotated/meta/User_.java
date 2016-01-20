package info.elexis.server.core.connector.elexis.jpa.model.annotated.meta;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Role;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.User;
import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.3.2.v20111125-r10461", date="2016-01-20T16:24:51")
@StaticMetamodel(User.class)
public class User_ { 

    public static volatile SingularAttribute<User, Boolean> administrator;
    public static volatile SingularAttribute<User, String> salt;
    public static volatile SingularAttribute<User, String> hashedPassword;
    public static volatile CollectionAttribute<User, Role> roles;
    public static volatile SingularAttribute<User, Kontakt> kontakt;
    public static volatile SingularAttribute<User, Boolean> active;
    public static volatile SingularAttribute<User, String> keystore;

}