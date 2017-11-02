package info.elexis.server.findings.fhir.jpa.model.annotated;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.0.v20160725-rNA")
@StaticMetamodel(ObservationLink.class)
public class ObservationLink_ extends AbstractDBObjectIdDeleted_ {

    public static volatile SingularAttribute<ObservationLink, String> sourceid;
    public static volatile SingularAttribute<ObservationLink, String> targetid;
    public static volatile SingularAttribute<ObservationLink, String> description;
    public static volatile SingularAttribute<ObservationLink, String> type;

}