package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.math.BigInteger;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AbstractDBObject.class)
public class AbstractDBObject_ {
	public static volatile SingularAttribute<AbstractDBObject, BigInteger> lastupdate;
}
