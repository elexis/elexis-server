package info.elexis.server.findings.fhir.jpa.model.annotated;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.0.v20160725-rNA")
@StaticMetamodel(Encounter.class)
public class Encounter_ extends AbstractDBObjectIdDeleted_ {

    public static volatile SingularAttribute<Encounter, String> patientid;
    public static volatile SingularAttribute<Encounter, String> mandatorid;
    public static volatile SingularAttribute<Encounter, String> consultationid;
    public static volatile SingularAttribute<Encounter, String> content;

}