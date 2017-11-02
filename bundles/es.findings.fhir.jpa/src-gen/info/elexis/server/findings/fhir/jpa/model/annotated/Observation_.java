package info.elexis.server.findings.fhir.jpa.model.annotated;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.0.v20160725-rNA")
@StaticMetamodel(Observation.class)
public class Observation_ extends AbstractDBObjectIdDeleted_ {

    public static volatile SingularAttribute<Observation, Boolean> referenced;
    public static volatile SingularAttribute<Observation, String> patientid;
    public static volatile SingularAttribute<Observation, String> originuri;
    public static volatile SingularAttribute<Observation, String> format;
    public static volatile SingularAttribute<Observation, String> decimalplace;
    public static volatile SingularAttribute<Observation, String> type;
    public static volatile SingularAttribute<Observation, String> encounterid;
    public static volatile SingularAttribute<Observation, String> performerid;
    public static volatile SingularAttribute<Observation, String> script;
    public static volatile SingularAttribute<Observation, String> content;

}