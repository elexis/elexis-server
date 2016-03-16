package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.2.v20151217-rNA", date="2016-03-16T15:28:30")
@StaticMetamodel(LabItem.class)
public class LabItem_ { 

    public static volatile SingularAttribute<LabItem, String> code;
    public static volatile SingularAttribute<LabItem, Boolean> visible;
    public static volatile SingularAttribute<LabItem, String> type;
    public static volatile SingularAttribute<LabItem, String> priority;
    public static volatile SingularAttribute<LabItem, String> loinccode;
    public static volatile SingularAttribute<LabItem, Kontakt> labor;
    public static volatile SingularAttribute<LabItem, String> referenceFemale;
    public static volatile SingularAttribute<LabItem, String> billingCode;
    public static volatile SingularAttribute<LabItem, String> referenceMale;
    public static volatile SingularAttribute<LabItem, String> unit;
    public static volatile SingularAttribute<LabItem, String> name;
    public static volatile SingularAttribute<LabItem, String> formula;
    public static volatile SingularAttribute<LabItem, Integer> digits;
    public static volatile SingularAttribute<LabItem, String> export;
    public static volatile SingularAttribute<LabItem, String> group;

}