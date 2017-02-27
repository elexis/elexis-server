package info.elexis.server.findings.fhir.jpa.model.annotated;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.0.v20160725-rNA")
@StaticMetamodel(Observation.class)
public class Observation_ { 

    public static volatile SingularAttribute<Observation, String> patientid;
    public static volatile SingularAttribute<Observation, String> encounterid;
    public static volatile SingularAttribute<Observation, String> performerid;
    public static volatile SingularAttribute<Observation, String> content;

}